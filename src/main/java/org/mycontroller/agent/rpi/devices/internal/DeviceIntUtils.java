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
package org.mycontroller.agent.rpi.devices.internal;

import java.util.HashMap;

import org.mycontroller.agent.rpi.AgentProperties;
import org.mycontroller.agent.rpi.model.DeviceInternal;
import org.mycontroller.agent.rpi.utils.AgentUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeviceIntUtils {
    public static final String KEY_CPU_TEMPERATURE = "cpu_temperature";
    public static final String KEY_CPU_TEMPERATURE_NAME = "CPU temperature";
    public static final String KEY_CPU_USAGE = "cpu_usage";
    public static final String KEY_CPU_USAGE_NAME = "CPU usage";
    public static final String KEY_CPU_VOLTAGE = "cpu_voltage";
    public static final String KEY_CPU_VOLTAGE_NAME = "CPU voltage";
    public static final String KEY_MEMORY_USAGE = "memory_usage";
    public static final String KEY_MEMORY_USAGE_NAME = "Memory usage";

    public static final HashMap<String, DeviceInternal> LOADED_DEVICES = new HashMap<String, DeviceInternal>();
    public static final HashMap<String, String> CLASSES_MAP = new HashMap<String, String>();
    static {
        CLASSES_MAP.put(KEY_CPU_TEMPERATURE, CpuTemperature.class.getName());
        CLASSES_MAP.put(KEY_CPU_VOLTAGE, CpuVoltage.class.getName());
        CLASSES_MAP.put(KEY_MEMORY_USAGE, SystemMemory.class.getName());
        CLASSES_MAP.put(KEY_CPU_USAGE, CpuUsage.class.getName());
    }

    public static McpRawMessage getPresentationMessage(String sensorId) {
        McpRawMessage message = AgentUtils.getMcpRawMessage();
        message.setNodeEui(AgentProperties.getInstance().getNodeNameInternal());
        message.setSensorId(sensorId);
        message.setMessageType(MESSAGE_TYPE.C_PRESENTATION);
        return message;
    }

    public static McpRawMessage getPayloadMessage(String sensorId) {
        McpRawMessage message = AgentUtils.getMcpRawMessage();
        message.setNodeEui(AgentProperties.getInstance().getNodeNameInternal());
        message.setSensorId(sensorId);
        message.setMessageType(MESSAGE_TYPE.C_SET);
        return message;
    }
}
