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

import org.mycontroller.agent.rpi.model.PWMOutputConf;
import org.mycontroller.agent.rpi.model.SoftPWMOutputConf;

import com.pi4j.io.gpio.GpioPinPwmOutput;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class PWMOutput extends SoftPWMOutput {
    @Override
    public GpioPinPwmOutput getProvisionedPin(SoftPWMOutputConf confOrg) {
        PWMOutputConf conf = (PWMOutputConf) confOrg;
        GpioPinPwmOutput _pin = (GpioPinPwmOutput) GPIO.getProvisionedPin(conf.getIoPin());
        if (_pin == null) {
            _pin = GPIO.provisionPwmOutputPin(conf.getIoPin());
            if (conf.getMode().equalsIgnoreCase("balanced")) {
                com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_BAL);
            } else {
                com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
            }
            com.pi4j.wiringpi.Gpio.pwmSetRange(conf.getRange());
            com.pi4j.wiringpi.Gpio.pwmSetClock(conf.getClock());
            _pin.setShutdownOptions(true);
        }
        return _pin;
    }
}
