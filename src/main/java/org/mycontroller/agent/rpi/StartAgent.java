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
package org.mycontroller.agent.rpi;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.mycontroller.agent.rpi.mqtt.AgentMessageMonitorThread;
import org.mycontroller.agent.rpi.mqtt.RpiMqttClient;
import org.mycontroller.agent.rpi.utils.AgentSchedulerUtils;
import org.mycontroller.agent.rpi.utils.AgentUtils;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiGpioProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class StartAgent {
    public static void main(String[] args) {
        try {
            startAgent();
        } catch (Exception ex) {
            _logger.error("Unable to start agent, refer error log,", ex);
            System.exit(1);//Terminate jvm, with non zero
        }
    }

    public static void startAgent() {
        long start = System.currentTimeMillis();
        loadInitialProperties();
        _logger.debug("App Properties: {}", AgentProperties.getInstance().toString());
        AboutAgent about = new AboutAgent();
        _logger.debug("{}", about);
        if (!about.getOsArch().equalsIgnoreCase("arm")) {
            _logger.error("This agent designed for Raspberry PI device. Stopped: Unable to run on this arch: '{}'",
                    about.getOsArch());
            System.exit(1);
        }
        startServices();
        _logger.info("McAgent started successfully in [{}] ms", System.currentTimeMillis() - start);
    }

    public static boolean loadInitialProperties() {
        String propertiesFile = System.getProperty("mca.conf.file");
        try {
            Properties properties = new Properties();
            if (propertiesFile == null) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mcagent.properties"));
            } else {
                properties.load(new FileReader(propertiesFile));
            }
            AgentProperties.getInstance().loadProperties(properties);
            _logger.debug("Properties are loaded successfuly...");
            return true;
        } catch (IOException ex) {
            _logger.error("Exception while loading properties file, ", ex);
            return false;
        }
    }

    public static void startServices() {
        // Start service order
        // - Add Shutdown hook
        // - Set pin numbering scheme
        // - start scheduler
        // - Start message Monitor Thread
        // - start device(s) services
        // - Start MQTT communication
        // - send device details

        //Add Shutdown hook
        new AgentShutdownHook().attachShutDownHook();

        //Set default pin numbering scheme
        GpioFactory.setDefaultProvider(new RaspiGpioProvider(AgentProperties.getInstance().getPinNumberingScheme()));

        //Start scheduler
        AgentSchedulerUtils.startScheduler();

        //Start message Monitor Thread
        //Create new thread to monitor received logs
        AgentMessageMonitorThread messageMonitorThread = new AgentMessageMonitorThread();
        Thread thread = new Thread(messageMonitorThread);
        thread.start();

        // Start device(s) services
        AgentUtils.loadDevices();

        // Start MQTT communication
        new Thread(RpiMqttClient.getInstance()).start();

        // Send device details
        AgentUtils.sendDeviceDetais();
    }

    public static void stopServices() {
        // Stop service order
        // - stop scheduler
        // - Stop MQTT communication
        // - Stop message Monitor Thread
        // - stop device(s) services

        // stop scheduler
        AgentSchedulerUtils.stop();

        // Stop MQTT communication
        RpiMqttClient.getInstance().stop();

        //Stop Message monitor thread
        AgentMessageMonitorThread.shutdown();

        //Stop device(s) service
        AgentUtils.removeAllListeners();
    }

}
