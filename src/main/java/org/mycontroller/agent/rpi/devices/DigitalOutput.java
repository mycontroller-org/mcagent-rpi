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

import org.mycontroller.agent.rpi.model.DigitalOutputConf;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class DigitalOutput extends DeviceBase {

    public static void setState(Pin ioPin, boolean state) {
        GpioPinDigitalOutput _pin = (GpioPinDigitalOutput) GPIO.getProvisionedPin(ioPin);
        if (_pin == null) {
            _pin = GPIO.provisionDigitalOutputPin(ioPin);
            _pin.setShutdownOptions(true);
        }
        if (state) {
            _pin.high();
        } else {
            _pin.low();
        }
    }

    public static void setState(DigitalOutputConf conf, boolean state) {
        _logger.debug("Changing pin state to >> {}, for {}", state, conf);
        setState(conf.getIoPin(), state);
    }

    public static int getState(DigitalOutputConf conf) {
        GpioPinDigitalOutput _pin = GPIO.provisionDigitalOutputPin(conf.getIoPin());
        return _pin.getState().getValue();
    }
}
