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

package com.zutubi.pulse.master.restore;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Constraint;

/**
 * Settings for automatic backups.  By default backups are enabled and run
 * daily at 5am.
 */
@SymbolicName("zutubi.backupConfig")
@Form(fieldOrder = {"enabled", "cronSchedule"})
@Classification(single = "backup")
public class BackupConfiguration extends AbstractConfiguration
{
    public static final String DEFAULT_CRON_SCHEDULE = "0 0 5 * * ?";

    @Constraint("com.zutubi.pulse.master.tove.config.project.triggers.CronExpressionValidator")
    private String cronSchedule = DEFAULT_CRON_SCHEDULE;

    @ControllingCheckbox(checkedFields = {"cronSchedule"})
    private boolean enabled = true;

    public BackupConfiguration()
    {
        setPermanent(true);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getCronSchedule()
    {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule)
    {
        this.cronSchedule = cronSchedule;
    }
}
