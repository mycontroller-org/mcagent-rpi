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
package org.mycontroller.agent.rpi.devices;

import org.mycontroller.agent.exceptions.ResourceNotAvailableException;
import org.mycontroller.agent.rpi.model.TemperatureDS18B20Conf;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;
import org.mycontroller.standalone.utils.McUtils;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class TemperatureDS18B20 extends W1GenericDevice {
    public String get(TemperatureDS18B20Conf conf, McpRawMessage message) throws ResourceNotAvailableException {
        switch (MESSAGE_TYPE_SET_REQ.valueOf(message.getSubType())) {
            case V_TEMP:
                return getTemperature(conf);
            case V_ID:
                return getId(conf);
            default:
                _logger.warn("This type of message not supported for {}, {}", conf, message);
                return null;
        }
    }

    public String getTemperature(TemperatureDS18B20Conf conf) throws ResourceNotAvailableException {
        TemperatureSensor sensor = (TemperatureSensor) getDevice(conf, TmpDS18B20DeviceType.FAMILY_CODE);
        if (sensor != null) {
            return McUtils.getDoubleAsString(sensor.getTemperature(conf.getScale()));
        }
        throw new ResourceNotAvailableException(conf.toString());
    }

    public String getId(TemperatureDS18B20Conf conf) {
        TemperatureSensor sensor = (TemperatureSensor) getDevice(conf, TmpDS18B20DeviceType.FAMILY_CODE);
        if (sensor != null) {
            return sensor.getName().replaceAll("(\\r|\\n)", "");
        }
        return null;
    }
}
