package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.license.LicenseManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.setup.SetupLicenseConfiguration;
import com.zutubi.pulse.master.tove.webwork.TransientAction;

/**
 */
public class SetupLicenseAction extends TransientAction<SetupLicenseConfiguration>
{
    private LicenseManager licenseManager;
    private String license;
    private SetupManager setupManager;

    public SetupLicenseAction()
    {
        super("init/license");
    }

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    protected SetupLicenseConfiguration initialise()
    {
        return new SetupLicenseConfiguration();
    }

    protected String complete(SetupLicenseConfiguration instance)
    {
        SecurityUtils.loginAsSystem();
        try
        {
            String licenseKey = instance.getLicense().replaceAll("\n", "");
            licenseManager.installLicense(licenseKey);
            setupManager.requestLicenseComplete();
        }
        finally
        {
            SecurityUtils.logout();
        }
        return SUCCESS;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
