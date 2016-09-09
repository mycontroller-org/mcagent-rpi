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
package org.mycontroller.agent.rpi.jobs;

import org.knowm.sundial.Job;
import org.knowm.sundial.JobContext;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.agent.rpi.model.IDeviceConf;
import org.mycontroller.agent.rpi.utils.AgentUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SendMeasurments extends Job {

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        IDeviceConf conf = (IDeviceConf) context.map.get(AgentUtils.KEY_GPIO_DEVICE_CONF);
        try {
            conf.sendMeasurments();
            _logger.debug("Measurment sent for {}", conf);
        } catch (Exception ex) {
            _logger.error("Exception, {}", conf, ex);
        }
    }

}
