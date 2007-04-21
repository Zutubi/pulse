package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.annotation.Wizard;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.setupConfig")
@Wizard("SetupWizard")
public class SetupConfiguration
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
