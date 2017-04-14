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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import org.quartz.CronScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;

/**
 */
public class CronExpressionValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String expression) throws ValidationException
    {
        if (expression == null)
        {
            expression = "";
        }
        try
        {
            OperableTrigger trigger = (OperableTrigger) TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                    .build();
            trigger.computeFirstFireTime(new BaseCalendar());
        }
        catch (Exception e)
        {
            addErrorMessage(e.getMessage());
        }
    }
}