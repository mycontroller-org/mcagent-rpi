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

import org.knowm.sundial.Job;
import org.knowm.sundial.JobContext;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.agent.rpi.model.DeviceInternal;
import org.mycontroller.agent.rpi.utils.AgentSchedulerUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.0.0
 */
@Slf4j
@NoArgsConstructor
public abstract class InternalBase extends Job implements IDeviceInternal {

    private DeviceInternal deviceInternal = null;

    public DeviceInternal deviceConfiguration() {
        return deviceInternal;
    }

    public void setDeviceConfiguration(DeviceInternal deviceInternal) {
        this.deviceInternal = deviceInternal;
    }

    @Override
    public void doRun() throws JobInterruptException {
        JobContext context = getJobContext();
        deviceInternal = (DeviceInternal) context.get(DeviceInternal.KEY_SELF);
        try {
            sendPayload();
        } catch (UnsupportedOperationException uex) {
            _logger.error("Received UnsupportedOperationException. Disabling this job: {}", context.getJobName(), uex);
            AgentSchedulerUtils.removeJob(context.getJobName());
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }

    }

    abstract void sendPayload();
}
