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

import java.util.Map;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Data
@ToString
public class DeviceInternal {
    public static final String KEY_SELF = "deviceInternal";
    public static final String CRON_EXPRESSION = "cron";
    private String id;
    private Boolean enabled;
    private Map<String, String> properties;

    public String getId() {
        if (id != null) {
            return id.replaceAll(" ", "_").trim();
        }
        return null;
    }
}
