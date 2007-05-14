package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("internal.setupConfig")
@Wizard("SetupWizard")
public class SetupConfiguration extends AbstractConfiguration
{
    private SetupDataConfiguration data;
    private SetupLicenseConfiguration license;
    private RequestLicenseConfiguration requestLicense;
    private AdminUserConfiguration admin;
    private ServerSettingsConfiguration server;

    public SetupDataConfiguration getData()
    {
        return data;
    }

    public void setData(SetupDataConfiguration data)
    {
        this.data = data;
    }

    public SetupLicenseConfiguration getLicense()
    {
        return license;
    }

    public void setLicense(SetupLicenseConfiguration license)
    {
        this.license = license;
    }

    public RequestLicenseConfiguration getRequestLicense()
    {
        return requestLicense;
    }

    public void setRequestLicense(RequestLicenseConfiguration requestLicense)
    {
        this.requestLicense = requestLicense;
    }

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
