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

import org.mycontroller.agent.rpi.utils.AgentUtils;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class AgentMessageMonitorThread implements Runnable {
    private static boolean terminationIssued = false;
    private static boolean terminated = false;
    public static final long MYS_MSG_DELAY = 20; // delay time to avoid collisions in network, in milliseconds

    public static boolean isTerminationIssued() {
        return terminationIssued;
    }

    public static synchronized void setTerminationIssued(boolean terminationIssued) {
        AgentMessageMonitorThread.terminationIssued = terminationIssued;
        long start = System.currentTimeMillis();
        long waitTime = McUtils.ONE_MINUTE;
        while (!terminated) {
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
        while (!RawMessageQueue.getInstance().isEmpty() && !isTerminationIssued()) {
            if (RpiMqttClient.getInstance().isRunning()) {
                RawMessage rawMessage = RawMessageQueue.getInstance().getMessage();
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
            while (!isTerminationIssued()) {
                try {
                    this.processRawMessage();
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    _logger.debug("Exception in sleep thread,", ex);
                }
            }
            if (!RawMessageQueue.getInstance().isEmpty()) {
                _logger.warn("MessageMonitorThread terminating with {} message(s) in queue!",
                        RawMessageQueue.getInstance().getQueueSize());
            }
            if (isTerminationIssued()) {
                _logger.debug("MessageMonitorThread termination issues. Terminating.");
                terminated = true;
            }
        } catch (Exception ex) {
            terminated = true;
            _logger.error("MessageMonitorThread terminated!, ", ex);
        }
    }

    public static boolean isTerminated() {
        return terminated;
    }

}
