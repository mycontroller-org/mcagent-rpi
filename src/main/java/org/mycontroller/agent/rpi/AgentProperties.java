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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.mycontroller.agent.rpi.model.Device;
import org.mycontroller.agent.rpi.model.DeviceInternal;
import org.mycontroller.agent.rpi.mqtt.RpiMqttProperties;
import org.mycontroller.standalone.utils.McUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.pi4j.io.gpio.RaspiPinNumberingScheme;
import com.pi4j.platform.Platform;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class AgentProperties {
    public static final String APPLICATION_NAME = "mcagent";
    public static final String NODE_INTERNAL = "Internal devices";
    public static final String NODE_GPIO = "GPIO devices";
    private static AgentProperties _instance = new AgentProperties();
    private RpiMqttProperties rpiMqttProperties = null;

    private String tmpLocation;
    private String devicesConfFile;
    private String appDirectory;
    private String nodeNameInternal;
    private String nodeNameGpio;
    private List<Device> devices;
    private List<DeviceInternal> devicesInternal;
    private RaspiPinNumberingScheme pinNumberingScheme;
    private Platform platform;

    public static AgentProperties getInstance() {
        return _instance;
    }

    public void loadProperties(Properties properties) throws IOException {
        //Application Directory
        try {
            appDirectory = McUtils.getDirectoryLocation(FileUtils.getFile(McUtils.getDirectoryLocation("../"))
                    .getCanonicalPath());
        } catch (IOException ex) {
            appDirectory = McUtils.getDirectoryLocation("../");
            _logger.error("Unable to set application directory!", ex);
        }
        //Create tmp location
        tmpLocation = McUtils.getDirectoryLocation(getValue(properties, "mcac.tmp.location", "/tmp"));

        createDirectoryLocation(tmpLocation);

        //Load node name internal and node name gpio
        nodeNameInternal = getValue(properties, "mcac.node.name.internal", "rpi-internal");
        nodeNameGpio = getValue(properties, "mcac.node.name.gpio", "rpi-gpio");

        // get/create devices configuration location
        devicesConfFile = getValue(properties, "mcac.devices.configuration", "../conf/devices.yaml");

        String feed = getValue(properties, "mcac.mqtt.feed", "rpiagent");
        String mqttTopicSubscribe = "in_" + feed;
        String mqttTopicPublish = "out_" + feed;

        rpiMqttProperties = RpiMqttProperties.builder()
                .brokerHost(getValue(properties, "mcac.mqtt.broker.host", "tcp://localhost:1883"))
                .clientId(getValue(properties, "mcac.mqtt.clientid", "rpi-agent"))
                .username(getValue(properties, "mcac.mqtt.username", ""))
                .password(getValue(properties, "mcac.mqtt.password", ""))
                .topicPublish(mqttTopicPublish)
                .topicSubscribe(mqttTopicSubscribe.endsWith("/#") ? mqttTopicSubscribe : mqttTopicSubscribe + "/#")
                .build();
        try {
            platform = Platform.valueOf(getValue(properties, "mcac.platform", "RASPBERRYPI").toUpperCase());
        } catch (Exception ex) {
            _logger.warn("Unknown platform! set to 'RASPBERRYPI'");
            platform = Platform.RASPBERRYPI;
        }

        try {
            pinNumberingScheme = RaspiPinNumberingScheme.valueOf(getValue(properties, "mcac.pin.numbering.scheme",
                    "DEFAULT_PIN_NUMBERING").toUpperCase());
        } catch (Exception ex) {
            _logger.warn("Unknown pin numbering scheme! set to 'DEFAULT_PIN_NUMBERING'");
            pinNumberingScheme = RaspiPinNumberingScheme.DEFAULT_PIN_NUMBERING;
        }

        //Load devices
        String yamlFile = FileUtils.readFileToString(FileUtils.getFile(getDevicesConfFile()));
        final Constructor deviceContructor = new Constructor(DevicesList.class);
        final TypeDescription deviceDescription = new TypeDescription(Device.class);
        deviceDescription.putMapPropertyType("gpio_devices", Device.class, Object.class);
        deviceDescription.putMapPropertyType("internal_devices", DeviceInternal.class, Object.class);
        deviceContructor.addTypeDescription(deviceDescription);
        final Yaml yaml = new Yaml(deviceContructor);
        final DevicesList devicesList = (DevicesList) yaml.load(yamlFile);
        this.devices = devicesList.getGpio_devices();
        this.devicesInternal = devicesList.getInternal_devices();
        _logger.debug("Devices: {}", devices);
        _logger.debug("DevicesInternal: {}", devicesInternal);
    }

    @Data
    public static class DevicesList {
        private List<Device> gpio_devices;
        private List<DeviceInternal> internal_devices;
    }

    private void createDirectoryLocation(String directoryLocation) {
        if (!FileUtils.getFile(directoryLocation).exists()) {
            if (FileUtils.getFile(directoryLocation).mkdirs()) {
                _logger.info("Created directory location: {}", directoryLocation);
            } else {
                _logger.error("Unable to create directory location: {}", directoryLocation);
            }
        }
    }

    private String getValue(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        _logger.debug("Key:{}-->{}", key, value);
        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }
}
