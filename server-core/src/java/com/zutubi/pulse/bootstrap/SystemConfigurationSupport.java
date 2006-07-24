package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.CompositeConfig;

/**
 * <class-comment/>
 */
public class SystemConfigurationSupport extends ConfigSupport implements SystemConfiguration
{
    public SystemConfigurationSupport(Config... config)
    {
        super(new CompositeConfig(config));
    }

    public int getServerPort()
    {
        return getInteger(WEBAPP_PORT, 8080);
    }

    public String getContextPath()
    {
        return getProperty(CONTEXT_PATH, "/");
    }
}
