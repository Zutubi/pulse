package com.zutubi.pulse.restore;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.backupConfig")
@Form(fieldOrder = {"enabled", "cronSchedule"})
public class BackupConfiguration extends AbstractConfiguration
{
    private static final String DEFAULT_CRON_SCHEDULE = "0 0 5 * * ?";

    private String cronSchedule = DEFAULT_CRON_SCHEDULE;

    @ControllingCheckbox
    private boolean enabled = false;

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
