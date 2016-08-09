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

import java.util.HashMap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentTimer {
    public static final String CRON_JOB_PREFIX = "cron_job_";
    public static final String INTERNAL_JOB_PREFIX = "internal_job_";
    public static final String JOB_PREFIX = "device_job_";
    private String jobName;
    private String cronExpression;
    private String targetClass;
    private HashMap<String, Object> data;

    public HashMap<String, Object> getData() {
        if (data == null) {
            data = new HashMap<String, Object>();
        }
        return data;
    }
}
