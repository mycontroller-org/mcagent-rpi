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

import java.io.IOException;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import com.pi4j.system.SystemInfo;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor
public class CpuVoltage extends Job implements IDeviceInternal {

    @Override
    public void doRun() throws JobInterruptException {
        try {
            sendPayload();
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }

    }

    private void sendPayload() {
        McpRawMessage message = getMcpRawMessage();
        message.setPayload(getVoltage());
        RawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    private String getVoltage() {
        try {
            return String.valueOf(SystemInfo.getCpuVoltage());
        } catch (NumberFormatException | UnsupportedOperationException | IOException | InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    @Override
    public void aboutMe() {
        McpRawMessage message = DeviceIntUtils.getPresentationMessage(DeviceIntUtils.KEY_CPU_VOLTAGE);
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_MULTIMETER.name());
        message.setPayload(DeviceIntUtils.KEY_CPU_VOLTAGE_NAME);
        RawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public McpRawMessage getMcpRawMessage() {
        McpRawMessage message = DeviceIntUtils.getPayloadMessage(DeviceIntUtils.KEY_CPU_VOLTAGE);
        message.setSubType(MESSAGE_TYPE_SET_REQ.V_VOLTAGE.name());
        return message;
    }

    @Override
    public void sendSensorVariables() {
        McpRawMessage message = this.getMcpRawMessage();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        RawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

}
