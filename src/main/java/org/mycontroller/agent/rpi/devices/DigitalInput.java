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

import org.mycontroller.agent.rpi.devices.listener.DigitalInputPinListener;
import org.mycontroller.agent.rpi.model.DigitalInputConf;

import com.pi4j.io.gpio.GpioPinDigitalInput;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DigitalInput extends DeviceBase {

    public static void listen(DigitalInputConf conf) {
        GpioPinDigitalInput listenPin = GPIO.provisionDigitalInputPin(conf.getIoPin(), conf.getPullResistance());
        listenPin.setShutdownOptions(true);
        if (conf.getDebounceInterval() != null) {
            listenPin.setDebounce(conf.getDebounceInterval());
        }
        listenPin.addListener(new DigitalInputPinListener(conf));
    }

    public static int getState(DigitalInputConf conf) {
        GpioPinDigitalInput listenPin = (GpioPinDigitalInput) GPIO.getProvisionedPin(conf.getIoPin());
        if (listenPin == null) {
            listen(conf);
            listenPin = (GpioPinDigitalInput) GPIO.getProvisionedPin(conf.getIoPin());
        }
        return listenPin.getState().getValue();
    }

}
