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

public enum INTERNAL_TYPE {
    CPU_TEMPERATURE("CPU temperature", CpuTemperature.class.getName()),
    CPU_USAGE("CPU usage", CpuUsage.class.getName()),
    CPU_VOLTAGE("CPU voltage", CpuVoltage.class.getName()),
    MEMORY_USAGE("Memory usage", SystemMemory.class.getName());

    private final String name;
    private final String className;

    private INTERNAL_TYPE(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public String getText() {
        return this.name;
    }

    public String getClassName() {
        return this.className;
    }

    public static INTERNAL_TYPE get(int id) {
        for (INTERNAL_TYPE type : values()) {
            if (type.ordinal() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.valueOf(id));
    }

    public static INTERNAL_TYPE fromString(String text) {
        if (text != null) {
            for (INTERNAL_TYPE type : INTERNAL_TYPE.values()) {
                if (text.equalsIgnoreCase(type.getText())) {
                    return type;
                }
            }
        }
        return null;
    }
}
