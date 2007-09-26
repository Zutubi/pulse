package com.zutubi.pulse.web.setup;

import com.zutubi.prototype.webwork.TransientAction;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.prototype.config.setup.SetupLicenseConfiguration;
import com.zutubi.pulse.security.AcegiUtils;

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
        AcegiUtils.loginAsSystem();
        try
        {
            String licenseKey = instance.getLicense().replaceAll("\n", "");
            licenseManager.installLicense(licenseKey);
            setupManager.requestLicenseComplete();
        }
        finally
        {
            AcegiUtils.logout();
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
