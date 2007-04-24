package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.Wizard;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.setupConfig")
@Wizard("SetupWizard")
public class SetupConfiguration extends AbstractConfiguration
{
    private AdminUserConfiguration admin;
    private ServerSettingsConfiguration server;

    public AdminUserConfiguration getAdmin()
    {
        return admin;
    }

    public void setAdmin(AdminUserConfiguration admin)
    {
        this.admin = admin;
    }

    public ServerSettingsConfiguration getServer()
    {
        return server;
    }

    public void setServer(ServerSettingsConfiguration server)
    {
        this.server = server;
    }
}
