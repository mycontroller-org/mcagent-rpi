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

import java.util.ArrayList;

import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class AgentRawMessageQueue {
    private ArrayList<RawMessage> rawMessagesQueue;
    private static final int INITAL_SIZE = 30;

    //Do not load until some calls getInstance
    private static class RawMessageQueueHelper {
        private static final AgentRawMessageQueue INSTANCE = new AgentRawMessageQueue();
    }

    public static AgentRawMessageQueue getInstance() {
        return RawMessageQueueHelper.INSTANCE;
    }

    private AgentRawMessageQueue() {
        rawMessagesQueue = new ArrayList<RawMessage>(INITAL_SIZE);
    }

    public synchronized void putMessage(RawMessage rawMessage) {
        rawMessagesQueue.add(rawMessage);
        _logger.debug("Added new {}, queue size:{}", rawMessage, rawMessagesQueue.size());
    }

    public synchronized RawMessage getMessage() {
        if (!rawMessagesQueue.isEmpty()) {
            RawMessage rawMessage = rawMessagesQueue.get(0);
            rawMessagesQueue.remove(0);
            _logger.debug("Removed a {}, queue size:{}", rawMessage, rawMessagesQueue.size());
            return rawMessage;
        } else {
            _logger.warn("There is no message in the queue, returning null");
            return null;
        }
    }

    public int getQueueSize() {
        return rawMessagesQueue.size();
    }

    public synchronized boolean isEmpty() {
        return rawMessagesQueue.isEmpty();
    }
}
