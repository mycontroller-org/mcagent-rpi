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
package org.mycontroller.agent.rpi.devices.internal;

import org.mycontroller.agent.rpi.mqtt.AgentRawMessageQueue;
import org.mycontroller.standalone.api.jaxrs.utils.StatusBase;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor
public class CpuUsage extends InternalBase {

    void sendPayload() {
        McpRawMessage message = getMcpRawMessage();
        message.setPayload(getUsage());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    private String getUsage() {
        try {
            return String.format("%.2f", StatusBase.operatingSystemMXBean.getSystemCpuLoad() * 100.0);
        } catch (NumberFormatException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    @Override
    public void aboutMe() {
        McpRawMessage message = DeviceIntUtils.getPresentationMessage(deviceConfiguration().getId());
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_CPU.name());
        message.setPayload(deviceConfiguration().getName());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public McpRawMessage getMcpRawMessage() {
        McpRawMessage message = DeviceIntUtils.getPayloadMessage(deviceConfiguration().getId());
        message.setSubType(MESSAGE_TYPE_SET_REQ.V_PERCENTAGE.name());
        return message;
    }

    @Override
    public void sendSensorVariables() {
        McpRawMessage message = getMcpRawMessage();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

}
