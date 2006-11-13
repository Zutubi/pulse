package com.zutubi.pulse.bootstrap.conf;

import com.zutubi.pulse.command.PulseCtl;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.util.FileSystemUtils;

/**
 * <class-comment/>
 */
public class EnvConfig extends ConfigSupport
{
    // we are leaving the definition of the PULSE_HOME property in the PulseCtl so that it does
    // not depend on a this class. PulseCtls dependencies need to be kept to a minimum.
    public static final String PULSE_HOME = PulseCtl.PULSE_HOME;
    public static final String VERSION_HOME = PulseCtl.VERSION_HOME;

    public static final String PULSE_CONFIG = "pulse.config";

    public static final String USER_HOME = "user.home";

    private String defaultConfig;
    private String defaultConfigDir;

    public EnvConfig(Config props)
    {
        super(props);
    }

    public boolean isWriteable()
    {
        return false;
    }

    public String getPulseHome()
    {
        return getProperty(EnvConfig.PULSE_HOME);
    }

    public boolean hasPulseHome()
    {
        return hasProperty(EnvConfig.PULSE_HOME);
    }

    public String getVersionHome()
    {
        return getProperty(EnvConfig.VERSION_HOME);
    }

    public boolean hasVersionHome()
    {
        return hasProperty(EnvConfig.VERSION_HOME);
    }

    public String getPulseConfig()
    {
        return getProperty(EnvConfig.PULSE_CONFIG);
    }

    public boolean hasPulseConfig()
    {
        return hasProperty(EnvConfig.PULSE_CONFIG);
    }

    public String getDefaultPulseConfigDir(String base)
    {
        if (defaultConfigDir == null)
        {
            defaultConfigDir = FileSystemUtils.composeFilename(getUserHome(), base);
        }
        return defaultConfigDir;

    }

    public String getDefaultPulseConfig(String base)
    {
        if (defaultConfig == null)
        {
            defaultConfig = FileSystemUtils.composeFilename(getDefaultPulseConfigDir(base), "config.properties");
        }
        return defaultConfig;
    }

    public String getUserHome()
    {
        return getProperty(com.zutubi.pulse.bootstrap.conf.EnvConfig.USER_HOME);
    }
}
