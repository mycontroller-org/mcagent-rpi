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

import java.io.IOException;

import org.knowm.sundial.JobContext;
import org.mycontroller.agent.rpi.model.DeviceInternal;
import org.mycontroller.agent.rpi.mqtt.AgentRawMessageQueue;
import org.mycontroller.agent.rpi.utils.AgentUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;
import org.mycontroller.standalone.utils.McUtils;

import com.pi4j.system.SystemInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SystemMemory extends InternalBase {
    public static final String KEY_UNIT = "unit";

    public static final String USED = MESSAGE_TYPE_SET_REQ.V_USED.name();
    public static final String FREE = MESSAGE_TYPE_SET_REQ.V_FREE.name();
    public static final String USED_PERCENTAGE = MESSAGE_TYPE_SET_REQ.V_PERCENTAGE.name();

    private Double divider = null;

    void sendPayload() {
        JobContext context = getJobContext();
        DeviceInternal device = (DeviceInternal) context.map.get(DeviceInternal.KEY_SELF);
        divider = AgentUtils.getDividerForData(device.getProperties().get(KEY_UNIT)).doubleValue();

        //Send Used
        McpRawMessage message = getMcpRawMessage();
        message.setSubType(USED);
        message.setPayload(getUsed());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //Send Free
        message.setSubType(FREE);
        message.setPayload(getFree());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //Send used percentage
        message.setSubType(USED_PERCENTAGE);
        message.setPayload(getUsedPercentage());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    private String getUsedPercentage() {
        try {
            return McUtils.getDoubleAsString((SystemInfo.getMemoryUsed() * 100.0) / SystemInfo.getMemoryTotal());
        } catch (NumberFormatException | IOException | InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    private String getUsed() {
        try {
            return McUtils.getDoubleAsString(SystemInfo.getMemoryUsed() / divider);
        } catch (NumberFormatException | IOException | InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    private String getFree() {
        try {
            return McUtils.getDoubleAsString(SystemInfo.getMemoryFree() / divider);
        } catch (NumberFormatException | IOException | InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    @Override
    public void aboutMe() {
        McpRawMessage message = DeviceIntUtils.getPresentationMessage(deviceConfiguration().getId());
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_MEMORY.name());
        message.setPayload(deviceConfiguration().getName());
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public McpRawMessage getMcpRawMessage() {
        McpRawMessage message = DeviceIntUtils.getPayloadMessage(deviceConfiguration().getId());
        return message;
    }

    @Override
    public void sendSensorVariables() {
        //used
        McpRawMessage message = getMcpRawMessage();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        message.setSubType(USED);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //free
        message.setSubType(FREE);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());

        //used percentage
        message.setSubType(USED_PERCENTAGE);
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

}
