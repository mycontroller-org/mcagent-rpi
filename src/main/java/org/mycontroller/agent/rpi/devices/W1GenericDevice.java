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
package org.mycontroller.agent.rpi.devices;

import java.util.List;

import org.mycontroller.agent.rpi.model.TemperatureDS18B20Conf;

import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public abstract class W1GenericDevice extends DeviceBase {
    private static final W1Master W1_MASTER = new W1Master();

    protected W1Master getW1Master() {
        return W1_MASTER;
    }

    protected W1Device getDevice(TemperatureDS18B20Conf conf, int familyCode) {
        List<W1Device> devices = getW1Master().getDevices(familyCode);
        _logger.debug("Number of temperature sensors:{}", devices.size());
        if (devices == null || devices.isEmpty()) {
            return null;
        }
        if (conf.getDeviceAddress() == null) {
            return devices.get(0);
        }
        for (W1Device device : devices) {
            _logger.debug("Device[id:[{}], name:[{}], familyId:[{}], class:[{}]]", device.getId(), device.getName(),
                    device.getFamilyId(), device.getClass().getName());
            String id = device.getId().replaceAll("(\\r|\\n)", "");
            if (id.equals(conf.getDeviceAddress())) {
                return device;
            }
        }
        return null;
    }

}
