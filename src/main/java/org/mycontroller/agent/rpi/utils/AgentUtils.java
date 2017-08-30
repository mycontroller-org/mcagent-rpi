/*
 * Copyright 2016-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.agent.rpi.utils;

import java.util.HashMap;

import org.mycontroller.agent.exceptions.ResourceNotAvailableException;
import org.mycontroller.agent.rpi.AboutAgent;
import org.mycontroller.agent.rpi.AgentProperties;
import org.mycontroller.agent.rpi.devices.DigitalInput;
import org.mycontroller.agent.rpi.devices.DigitalOutput;
import org.mycontroller.agent.rpi.devices.PWMOutput;
import org.mycontroller.agent.rpi.devices.SoftPWMOutput;
import org.mycontroller.agent.rpi.devices.TemperatureDS18B20;
import org.mycontroller.agent.rpi.devices.internal.DeviceIntUtils;
import org.mycontroller.agent.rpi.devices.internal.IDeviceInternal;
import org.mycontroller.agent.rpi.devices.internal.INTERNAL_TYPE;
import org.mycontroller.agent.rpi.jobs.SendMeasurments;
import org.mycontroller.agent.rpi.model.AgentTimer;
import org.mycontroller.agent.rpi.model.Device;
import org.mycontroller.agent.rpi.model.DeviceInternal;
import org.mycontroller.agent.rpi.model.DigitalInputConf;
import org.mycontroller.agent.rpi.model.DigitalOutputConf;
import org.mycontroller.agent.rpi.model.IDeviceConf;
import org.mycontroller.agent.rpi.model.PWMOutputConf;
import org.mycontroller.agent.rpi.model.SoftPWMOutputConf;
import org.mycontroller.agent.rpi.model.TemperatureDS18B20Conf;
import org.mycontroller.agent.rpi.mqtt.AgentRawMessageQueue;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.mycontroller.standalone.provider.mc.McpRawMessage;
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
public class AgentUtils {
    private static final HashMap<String, IDeviceConf> DEVICES_MAP = new HashMap<>();
    public static final String KEY_GPIO_DEVICE_CONF = "gpioDeviceConf";

    public static Long getDividerForData(String unit) {
        if (unit == null) {
            return 1L;
        } else if (unit.equalsIgnoreCase("KB")) {
            return McUtils.KB;
        } else if (unit.equalsIgnoreCase("MB")) {
            return McUtils.MB;
        } else if (unit.equalsIgnoreCase("GB")) {
            return McUtils.GB;
        } else if (unit.equalsIgnoreCase("TB")) {
            return McUtils.TB;
        }
        return 1L;
    }

    public enum DEVICE_TYPE {
        DIGITAL_OUT,
        DIGITAL_IN,
        SOFT_PWM_OUT,
        PWM_OUT,
        TEMPERATURE_DS18B20;

        public static DEVICE_TYPE get(int id) {
            for (DEVICE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    public static IDeviceConf getDeviceConf(String key) {
        return DEVICES_MAP.get(key);
    }

    private static void loadInternalDevice() {
        for (DeviceInternal device : AgentProperties.getInstance().getDevicesInternal()) {
            if (device.getEnabled() == null || !device.getEnabled()) {
                _logger.debug("This device is in disabled state! {}", device);
                continue;
            }
            if (device.getType() == null) {
                _logger.warn("Please check the type! unknown internal device! {}", device);
                continue;
            }
            if (DeviceIntUtils.LOADED_DEVICES.get(device.getType().getText()) != null) {
                _logger.warn("Found duplicate entry for {}", device);
                continue;
            }
            AgentTimer timer = AgentTimer.builder()
                    .jobName(device.getId())
                    .cronExpression(device.getProperties().get(DeviceInternal.CRON_EXPRESSION))
                    .targetClass(device.getType().getClassName())
                    .build();
            timer.getData().put(DeviceInternal.KEY_SELF, device);
            AgentSchedulerUtils.addJob(timer);
            DeviceIntUtils.LOADED_DEVICES.put(device.getType().getText(), device);
            _logger.debug("This device loaded successfully, {}", device);
        }
    }

    private static void loadGpioDevice() {
        for (Device device : AgentProperties.getInstance().getDevices()) {
            _logger.debug("{}", device);
            DEVICE_TYPE type = DEVICE_TYPE.valueOf(device.getType().toUpperCase().replaceAll(" ", "_"));
            IDeviceConf deviceConf = null;
            if (device.getEnabled() == null || !device.getEnabled()) {
                _logger.debug("This device is in disabled state! {}", device);
                continue;
            }
            if (type != null && DEVICES_MAP.get(device.getId()) == null) {
                switch (type) {
                    case DIGITAL_IN:
                        deviceConf = new DigitalInputConf(device);
                        new DigitalInput().listen((DigitalInputConf) deviceConf);
                        break;
                    case DIGITAL_OUT:
                        deviceConf = new DigitalOutputConf(device);
                        break;
                    case SOFT_PWM_OUT:
                        deviceConf = new SoftPWMOutputConf(device);
                        break;
                    case PWM_OUT:
                        deviceConf = new PWMOutputConf(device);
                        break;
                    case TEMPERATURE_DS18B20:
                        deviceConf = new TemperatureDS18B20Conf(device);
                        break;
                    default:
                        break;

                }
                if (deviceConf != null) {
                    DEVICES_MAP.put(deviceConf.getId(), deviceConf);
                    if (deviceConf.getCron() != null) {
                        AgentTimer timer = AgentTimer.builder()
                                .jobName(device.getId())
                                .cronExpression(deviceConf.getCron())
                                .targetClass(SendMeasurments.class.getName())
                                .build();
                        timer.getData().put(KEY_GPIO_DEVICE_CONF, deviceConf);
                        AgentSchedulerUtils.addJob(timer);
                    }
                }
            } else if (DEVICES_MAP.get(device.getId()) != null) {
                _logger.error("With this id a device already available! {}", device);
            }
        }
    }

    public static void loadDevices() {
        loadGpioDevice();
        loadInternalDevice();
    }

    public static void removeAllListeners() {
        DigitalInput.removeAllListeners();
    }

    public static McpRawMessage getMcpRawMessage() {
        McpRawMessage message = new McpRawMessage(AgentProperties.getInstance().getRpiMqttProperties()
                .getTopicPublish());
        message.setTxMessage(true);
        message.setPayload(McpUtils.EMPTY_DATA);
        return message;
    }

    public static void processReceivedMessage(RawMessage rawMessage) throws RawMessageException {
        McpRawMessage message = new McpRawMessage(rawMessage);
        if (message.getAck() == McMessage.ACK_REQUEST) {
            message.setAck(McMessage.NO_ACK);//Reset ack request
            McpRawMessage _msg = message.clone();//Create new msg to respond ack
            _msg.setAck(McMessage.ACK_RESPONSE);
            _msg.setTopicsPublish(AgentProperties.getInstance().getRpiMqttProperties().getTopicPublish());
            _msg.setTxMessage(true);
            AgentRawMessageQueue.getInstance().putMessage(_msg.getRawMessage());
        } else if (message.getAck() == McMessage.ACK_RESPONSE) {
            _logger.debug("This is ack response message: {}", message);
            return;
        }
        IDeviceConf deviceConf = getDeviceConf(message.getSensorId());
        if (deviceConf == null
                && (message.getMessageType() == MESSAGE_TYPE.C_SET || message.getMessageType() == MESSAGE_TYPE.C_REQ)) {
            _logger.warn("Message received for unknown device '{}', {}", message.getSensorId(), message);
            return;
        }
        switch (message.getMessageType()) {
            case C_INTERNAL:
                processInternal(message);
                break;
            case C_PRESENTATION:
                processPresentation(message);
                break;
            case C_REQ:
                processReqType(message, deviceConf);
                break;
            case C_SET:
                processSetType(message, deviceConf);
                break;
            default:
                _logger.warn("Received not supported message type: {}", message);
                break;
        }
    }

    private static void processSetType(McpRawMessage message, IDeviceConf deviceConf) {
        switch (deviceConf.getType()) {
            case DIGITAL_IN:
                //Nothing to do
                break;
            case DIGITAL_OUT:
                new DigitalOutput().setState((DigitalOutputConf) deviceConf, McUtils.getBoolean(message.getPayload()));
                break;
            case SOFT_PWM_OUT:
                new SoftPWMOutput().set((SoftPWMOutputConf) deviceConf, message);
                break;
            case PWM_OUT:
                new PWMOutput().set((PWMOutputConf) deviceConf, message);
                break;
            default:
                _logger.warn("Not supported type:{}, {}", message, deviceConf);
                break;

        }
    }

    private static void processReqType(McpRawMessage message, IDeviceConf deviceConf) {
        try {
            switch (deviceConf.getType()) {
                case DIGITAL_IN:
                    message.setPayload(String.valueOf(new DigitalInput().getState((DigitalInputConf) deviceConf)));
                    break;
                case DIGITAL_OUT:
                    message.setPayload(String.valueOf(new DigitalOutput().getState((DigitalOutputConf) deviceConf)));
                    break;
                case SOFT_PWM_OUT:
                    message.setPayload(new SoftPWMOutput().get((SoftPWMOutputConf) deviceConf, message));
                    break;
                case PWM_OUT:
                    message.setPayload(new PWMOutput().get((PWMOutputConf) deviceConf, message));
                    break;
                case TEMPERATURE_DS18B20:
                    message.setPayload(new TemperatureDS18B20().get((TemperatureDS18B20Conf) deviceConf, message));
                    break;
                default:
                    break;
            }
            message.setTxMessage(true);
            message.setMessageType(MESSAGE_TYPE.C_SET);
            message.setTopicsPublish(AgentProperties.getInstance().getRpiMqttProperties().getTopicPublish());
            AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
        } catch (ResourceNotAvailableException ex) {
            _logger.error("Exception,", ex);
        }

    }

    private static void processInternal(McpRawMessage message) {
        switch (MESSAGE_TYPE_INTERNAL.valueOf(message.getSubType())) {
            case I_BATTERY_LEVEL:
                break;
            case I_DEBUG:
                break;
            case I_HEARTBEAT:
                genericResponse(message, MESSAGE_TYPE_INTERNAL.I_HEARTBEAT_RESPONSE.name());
                break;
            case I_ID_RESPONSE:
                break;
            case I_LOG_MESSAGE:
                break;
            case I_PING:
                genericResponse(message, MESSAGE_TYPE_INTERNAL.I_PONG.name());
                break;
            case I_DISCOVER:
            case I_PRESENTATION:
                sendDeviceDetais();
                break;
            case I_REBOOT:
                break;
            case I_TIME:
                break;
            default:
                _logger.warn("Not supported type:{}", message);
                break;
        }
    }

    private static void processPresentation(McpRawMessage message) {
        switch (MESSAGE_TYPE_PRESENTATION.valueOf(message.getSubType())) {
            default:
                //Nothing to do.
                break;
        }
    }

    private static void genericResponse(McpRawMessage message, String subType) {
        message.setTxMessage(true);
        message.setTopicsPublish(AgentProperties.getInstance().getRpiMqttProperties().getTopicPublish());
        message.setPayload(String.valueOf(System.currentTimeMillis()));
        message.setSubType(subType);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    private static void sendNodeInformation() {
        AboutAgent about = new AboutAgent();
        McpRawMessage message = getMcpRawMessage();
        //Send Internal and gpio node details
        message.setMessageType(MESSAGE_TYPE.C_INTERNAL);
        message.setSubType(MESSAGE_TYPE_INTERNAL.I_SKETCH_NAME.name());
        message.setNodeEui(AgentProperties.getInstance().getNodeNameInternal());
        message.setSensorId(McMessage.SENSOR_BROADCAST_ID);

        //Send node name
        message.setPayload(AgentProperties.NODE_INTERNAL);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        message.setNodeEui(AgentProperties.getInstance().getNodeNameGpio());
        message.setPayload(AgentProperties.NODE_GPIO);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //Send node version
        message.setSubType(MESSAGE_TYPE_INTERNAL.I_SKETCH_VERSION.name());
        message.setPayload(about.getVersion());
        message.setNodeEui(AgentProperties.getInstance().getNodeNameInternal());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        message.setNodeEui(AgentProperties.getInstance().getNodeNameGpio());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //Send node lib version
        message.setMessageType(MESSAGE_TYPE.C_PRESENTATION);
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_ARDUINO_NODE.name());
        message.setPayload(about.getLibVersion());
        message.setNodeEui(AgentProperties.getInstance().getNodeNameInternal());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        message.setNodeEui(AgentProperties.getInstance().getNodeNameGpio());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

    }

    public static void sendDeviceDetais() {
        //Send node details
        sendNodeInformation();

        //Send sensor details
        for (String deviceId : DEVICES_MAP.keySet()) {
            IDeviceConf conf = getDeviceConf(deviceId);
            conf.aboutMe();
            conf.sendSensorTypes();
        }

        //Send internal sensors details
        _logger.debug("Available intenal devices: {}", DeviceIntUtils.LOADED_DEVICES);
        for (String deviceTypeText : DeviceIntUtils.LOADED_DEVICES.keySet()) {
            _logger.debug("Sending poresentation data for '{}'", deviceTypeText);
            try {
                Class<?> clazz = Class.forName(INTERNAL_TYPE.fromString(deviceTypeText).getClassName());
                IDeviceInternal device = (IDeviceInternal) clazz.newInstance();
                device.setDeviceConfiguration(DeviceIntUtils.LOADED_DEVICES.get(deviceTypeText));
                device.aboutMe();
                device.sendSensorVariables();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                _logger.error("Exception, ", ex);
            }
        }
    }
}
