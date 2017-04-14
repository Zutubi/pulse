/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.bean.ObjectFactory;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * This implementation of the {@link JobFactory} interface
 * ensures that all of the jobs created for Quartz are correctly
 * wired.
 */
public class QuartzTaskJobFactory implements JobFactory
{
    private ObjectFactory objectFactory;

    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException
    {
        Class jobClass = bundle.getJobDetail().getJobClass();
        if (jobClass == QuartzTaskCallbackJob.class)
        {
            // The QuartzTaskCallbackJob is a special case because it is
            // wrapped by a transaction proxy.  If we request the bean
            // by class, then the unwrapped version is returned.
            return SpringComponentContext.getBean("quartzTaskCallbackJob");
        }
        return (Job) objectFactory.buildBean(jobClass);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
