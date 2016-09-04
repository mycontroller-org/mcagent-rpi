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

import org.mycontroller.agent.rpi.model.SoftPWMOutputConf;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mc.McpRawMessage;
import org.mycontroller.standalone.utils.McUtils;

import com.pi4j.io.gpio.GpioPinPwmOutput;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SoftPWMOutput extends DeviceBase {
    public GpioPinPwmOutput getProvisionedPin(SoftPWMOutputConf conf) {
        GpioPinPwmOutput _pin = (GpioPinPwmOutput) GPIO.getProvisionedPin(conf.getIoPin());
        if (_pin == null) {
            _pin = GPIO.provisionSoftPwmOutputPin(conf.getIoPin());
            _pin.setPwmRange(conf.getRange());
            _pin.setShutdownOptions(true);
        }
        return _pin;
    }

    public void setRate(SoftPWMOutputConf conf, int rate) {
        int oldRate = getRate(conf);
        GpioPinPwmOutput _pin = getProvisionedPin(conf);
        if (rate > conf.getRange()) {
            _logger.warn("input rate out of range: {} set to {}", rate, conf.getRange());
            rate = conf.getRange();
        } else if (rate < 0) {
            _logger.warn("input rate out of range: {} set to 0", rate);
            rate = 0;
        }
        _pin.setPwm(rate);
        int newRate = getRate(conf);
        _logger.debug("Rate changed from [{}] to [{}] for {}. User input:{}", oldRate, newRate, conf, rate);
    }

    public void set(SoftPWMOutputConf conf, McpRawMessage message) {
        switch (MESSAGE_TYPE_SET_REQ.valueOf(message.getSubType())) {
            case V_RATE:
                setRate(conf, McUtils.getInteger(message.getPayload()));
                break;
            default:
                _logger.warn("This type of message not supported for {}, {}", conf, message);
                break;
        }
    }

    public String get(SoftPWMOutputConf conf, McpRawMessage message) {
        switch (MESSAGE_TYPE_SET_REQ.valueOf(message.getSubType())) {
            case V_RATE:
                return String.valueOf(getRate(conf));
            default:
                _logger.warn("This type of message not supported for {}, {}", conf, message);
                return null;
        }
    }

    public int getRate(SoftPWMOutputConf conf) {
        GpioPinPwmOutput _pin = getProvisionedPin(conf);
        return _pin.getPwm();
    }
}
