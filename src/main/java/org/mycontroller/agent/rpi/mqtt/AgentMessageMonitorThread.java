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
package org.mycontroller.agent.rpi.mqtt;

import java.util.concurrent.atomic.AtomicBoolean;

import org.mycontroller.agent.rpi.utils.AgentUtils;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class AgentMessageMonitorThread implements Runnable {
    private static final AtomicBoolean TERMINATION_ISSUED = new AtomicBoolean(false);
    private static final AtomicBoolean TERMINATED = new AtomicBoolean(false);

    public static synchronized void shutdown() {
        TERMINATION_ISSUED.set(true);
        long start = System.currentTimeMillis();
        long waitTime = McUtils.ONE_MINUTE;
        while (!TERMINATED.get()) {
            try {
                Thread.sleep(10);
                if ((System.currentTimeMillis() - start) >= waitTime) {
                    _logger.warn("Unable to stop MessageMonitorThread on specied wait time[{}ms]", waitTime);
                    break;
                }
            } catch (InterruptedException ex) {
                _logger.debug("Exception in xsleep thread,", ex);
            }
        }
        _logger.debug("MessageMonitorThread terminated");
    }

    private void processRawMessage() {
        while (!AgentRawMessageQueue.getInstance().isEmpty() && !TERMINATION_ISSUED.get()) {
            if (RpiMqttClient.getInstance().isRunning()) {
                RawMessage rawMessage = AgentRawMessageQueue.getInstance().getMessage();
                try {
                    _logger.debug("Processing message:[{}]", rawMessage);
                    if (rawMessage.isTxMessage()) {
                        RpiMqttClient.getInstance().publish(rawMessage.getSubData(),
                                String.valueOf(rawMessage.getData()));
                    } else {
                        AgentUtils.processReceivedMessage(rawMessage);
                    }
                } catch (Exception ex) {
                    _logger.error("Exception on processing [{}], ", rawMessage, ex);
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                    _logger.error("Exception, ", ex);
                }
            }

        }
    }

    @Override
    public void run() {
        try {
            _logger.debug("MessageMonitorThread new thread started.");
            while (!TERMINATION_ISSUED.get()) {
                try {
                    this.processRawMessage();
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    _logger.debug("Exception in sleep thread,", ex);
                }
            }
            if (!AgentRawMessageQueue.getInstance().isEmpty()) {
                _logger.warn("MessageMonitorThread terminating with {} message(s) in queue!",
                        AgentRawMessageQueue.getInstance().getQueueSize());
            }
            if (TERMINATION_ISSUED.get()) {
                _logger.debug("MessageMonitorThread termination issues. Terminating.");
                TERMINATED.set(true);
            }
        } catch (Exception ex) {
            TERMINATED.set(true);
            _logger.error("MessageMonitorThread terminated!, ", ex);
        }
    }
}
