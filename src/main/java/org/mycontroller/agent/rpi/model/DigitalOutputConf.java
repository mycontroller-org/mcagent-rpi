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

import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import com.pi4j.io.gpio.Pin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class DigitalOutputConf extends DeviceConf {
    private Pin ioPin;

    public DigitalOutputConf(Device device) {
        super(device);
        ioPin = super.getPinByName(device.getProperties().get(KEY_PIN));
    }

    @Override
    public void aboutMe() {
        McpRawMessage message = super.getPresentationMessage();
        message.setSubType(MESSAGE_TYPE_PRESENTATION.S_BINARY.name());
        message.setPayload(getName());
        RawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public void sendSensorTypes() {
        McpRawMessage message = getMcpRawMessage();
        message.setMessageType(MESSAGE_TYPE.C_REQ);
        RawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

    @Override
    public McpRawMessage getMcpRawMessage() {
        McpRawMessage message = super.getPayloadMessage();
        message.setSubType(MESSAGE_TYPE_SET_REQ.V_STATUS.name());
        return message;
    }
}
