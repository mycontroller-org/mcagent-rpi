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
package org.mycontroller.agent.rpi.utils;

import org.knowm.sundial.SundialJobScheduler;
import org.mycontroller.agent.rpi.model.AgentTimer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentSchedulerUtils {
    public static final String TRIGGER_PREFIX = "trigger_prefix_";

    public static void startScheduler() {
        SundialJobScheduler.startScheduler();
        _logger.debug("SundialJobScheduler started.Jobs:[{}]",
                SundialJobScheduler.getAllJobNames());
    }

    public static void stop() {
        if (SundialJobScheduler.getScheduler() != null) {
            SundialJobScheduler.shutdown();
        }
        _logger.debug("Scheduler stopped...");
    }

    public static void addJob(AgentTimer timer) {
        //Add a job
        SundialJobScheduler.addJob(
                timer.getJobName(),
                timer.getTargetClass(),
                timer.getData(),
                false);
        //Add a cron for this job
        SundialJobScheduler.addCronTrigger(TRIGGER_PREFIX + timer.getJobName(), timer.getJobName(),
                timer.getCronExpression(), null, null);
        _logger.debug("New timer job added, {}", timer);
    }

    public static void removeJob(String jobName) {
        SundialJobScheduler.removeTrigger(TRIGGER_PREFIX + jobName);
        SundialJobScheduler.removeJob(jobName);
    }
}
