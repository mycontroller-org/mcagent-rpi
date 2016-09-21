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
package org.mycontroller.agent.rpi.model;

import java.util.Map;

import org.mycontroller.agent.rpi.AgentProperties;
import org.mycontroller.agent.rpi.utils.AgentUtils;
import org.mycontroller.agent.rpi.utils.AgentUtils.DEVICE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiBcmPin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.RaspiPinNumberingScheme;

import lombok.Getter;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Getter
@ToString
abstract class DeviceConf implements IDeviceConf {
    public static final String KEY_PIN = "pin";
    public static final String CRON_EXPRESSION = "cron";

    private String id;
    private String name;
    private DEVICE_TYPE type;
    private String cron;

    public DeviceConf(Device device) {
        id = device.getId();
        name = device.getName();
        type = DEVICE_TYPE.valueOf(device.getType().toUpperCase().replaceAll(" ", "_"));
        cron = getValue(device.getProperties(), CRON_EXPRESSION);
    }

    protected Pin getPinByName(String pinName) {
        if (AgentProperties.getInstance().getPinNumberingScheme() == RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING) {
            return RaspiBcmPin.getPinByName(pinName.toUpperCase());
        } else {
            return RaspiPin.getPinByName(pinName.toUpperCase());
        }
    }

    protected McpRawMessage getPresentationMessage() {
        McpRawMessage message = AgentUtils.getMcpRawMessage();
        message.setNodeEui(AgentProperties.getInstance().getNodeNameGpio());
        message.setSensorId(getId());
        message.setMessageType(MESSAGE_TYPE.C_PRESENTATION);
        return message;
    }

    protected McpRawMessage getPayloadMessage() {
        McpRawMessage message = AgentUtils.getMcpRawMessage();
        message.setNodeEui(AgentProperties.getInstance().getNodeNameGpio());
        message.setSensorId(getId());
        message.setMessageType(MESSAGE_TYPE.C_SET);
        return message;
    }

    public McpRawMessage getMcpRawMessage(MESSAGE_TYPE_SET_REQ setReqType) {
        McpRawMessage message = getPayloadMessage();
        message.setSubType(setReqType.name());
        return message;
    }

    protected String getValue(Map<String, String> properties, String key) {
        return getValue(properties, key, null);
    }

    protected String getValue(Map<String, String> properties, String key, String defaultValue) {
        if (properties.get(key) != null) {
            return properties.get(key);
        }
        return defaultValue;
    }

    public void sendMeasurments() {
        //Override on child classes
    }
}
