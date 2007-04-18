package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.config.CompositeConfig;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;

/**
 */
public class MasterConfigurationSupport extends ConfigSupport implements MasterConfiguration
{
    public MasterConfigurationSupport(Config... config)
    {
        super(new CompositeConfig(config));
    }

    public String getAdminLogin()
    {
        return getProperty(ADMIN_LOGIN);
    }

    public void setAdminLogin(String login)
    {
        setProperty(ADMIN_LOGIN, login);
    }

    public boolean isMasterEnabled()
    {
        return getBooleanProperty(MASTER_ENABLED, Boolean.TRUE);
    }

    public void setMasterEnabled(Boolean b)
    {
        setBooleanProperty(MASTER_ENABLED, b);
    }
}
