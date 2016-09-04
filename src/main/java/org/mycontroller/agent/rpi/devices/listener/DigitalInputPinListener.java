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
package org.mycontroller.agent.rpi.devices.listener;

import org.mycontroller.agent.rpi.model.DigitalInputConf;
import org.mycontroller.agent.rpi.mqtt.AgentRawMessageQueue;
import org.mycontroller.standalone.provider.mc.McpRawMessage;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class DigitalInputPinListener implements GpioPinListenerDigital {
    private DigitalInputConf conf;

    public DigitalInputPinListener(DigitalInputConf conf) {
        this.conf = conf;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent stateEvent) {
        _logger.debug("Pin state:{}, ", stateEvent.getState().getValue(), conf);
        //Send to message queue
        McpRawMessage message = conf.getMcpRawMessage();
        AgentRawMessageQueue.getInstance().putMessage(message.getRawMessage());
    }

}
