/*
 * Copyright 2016 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.agent.rpi.mqtt;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mycontroller.agent.rpi.AgentProperties;
import org.mycontroller.standalone.provider.mc.McpUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RpiMqttClient implements Runnable {

    private static RpiMqttClient _instance = new RpiMqttClient();

    public static final long DISCONNECT_TIME_OUT = 1000 * 3;
    public static final int CONNECTION_TIME_OUT = 1000 * 5;
    public static final int KEEP_ALIVE = 1000 * 5;
    public static final int AGENT_QOS = 0;
    public static final long RETRY_TIME = McUtils.SECOND * 15;

    private IMqttClient mqttClient;
    private RpiMqttCallbackListener rpiMqttCallbackListener;
    private boolean isRunning = false;
    private boolean terminate = false;

    public static RpiMqttClient getInstance() {
        return _instance;
    }

    public void startMqttClient() {
        if (isRunning()) {
            _logger.info("MQTT client already running. Nothing to do..");
            return;
        }
        try {
            RpiMqttProperties _properties = AgentProperties.getInstance().getRpiMqttProperties();
            mqttClient = new MqttClient(_properties.getBrokerHost(), _properties.getClientId() + "_"
                    + RandomStringUtils.randomAlphanumeric(5));
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (_properties.getUsername() != null && _properties.getUsername().length() > 0) {
                connectOptions.setUserName(_properties.getUsername());
                connectOptions.setPassword(_properties.getPassword().toCharArray());
            }
            mqttClient.connect(connectOptions);
            rpiMqttCallbackListener = new RpiMqttCallbackListener(mqttClient, connectOptions);
            mqttClient.setCallback(rpiMqttCallbackListener);
            mqttClient.subscribe(_properties.getTopicSubscribe());
            _logger.info("MQTT client[server: {}, topicSubscribe:{}] connected successfully..",
                    mqttClient.getServerURI(), _properties.getTopicSubscribe());
            isRunning = true;
        } catch (MqttException ex) {
            _logger.error("Unable to connect with MQTT broker [{}], Reason Code: {}, ",
                    mqttClient.getServerURI(), ex.getReasonCode(), ex);
        }
    }

    public synchronized void publish(String topic, String payload) {
        if (payload == null) {
            payload = McpUtils.EMPTY_DATA;
        }
        _logger.debug("Message about to send, Topic:[{}], Payload:[{}]", topic, payload);
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(AGENT_QOS);
            mqttClient.publish(topic, message);
        } catch (MqttException ex) {
            if (ex.getMessage().contains("Timed out waiting for a response from the server")) {
                _logger.debug(ex.getMessage());
            } else {
                _logger.error("Exception, Reason Code:{}", ex.getReasonCode(), ex);
            }
        }
    }

    public void stop() {
        try {
            if (rpiMqttCallbackListener != null) {
                rpiMqttCallbackListener.stopReconnect();
            }
            if (mqttClient != null) {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect(DISCONNECT_TIME_OUT);
                }
                mqttClient.close();
            }
            isRunning = false;
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        try {
            while (!isTerminate() && !isRunning()) {
                startMqttClient();
                long waitTime = RETRY_TIME;
                while (!isTerminate() && waitTime > 0) {
                    try {
                        Thread.sleep(100);
                        waitTime -= 100;
                    } catch (InterruptedException ex) {
                        _logger.error("Error,", ex);
                    }
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }

    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

}
