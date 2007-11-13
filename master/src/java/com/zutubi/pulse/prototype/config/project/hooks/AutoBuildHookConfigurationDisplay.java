package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.config.LicenseConfiguration;
import com.zutubi.i18n.Messages;

import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 */
public class AutoBuildHookConfigurationDisplay
{
    public String getState(AutoBuildHookConfiguration hook)
    {
        return hook.isEnabled() ? "enabled" : "disabled";
    }
}
