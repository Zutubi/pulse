package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Home;
import com.zutubi.pulse.web.ActionSupport;

import java.io.IOException;

/**
 * The manage license action supports updating the license currently installed
 * in this system.
 *
 * @author Daniel Ostermeier
 */
public class ManageLicenseAction extends ActionSupport
{
    private ConfigurationManager configurationManager;

    /**
     * The license key
     */
    private String license;

    /**
     * @see ManageLicenseAction#license
     */
    public String getLicense()
    {
        return license;
    }

    /**
     * @see ManageLicenseAction#license
     */
    public void setLicense(String license)
    {
        this.license = license;
    }

    public String doSave()
    {
        return execute();
    }

    public String execute()
    {
        // update the license string.
        Home home = configurationManager.getHome();

        try
        {
            home.updateLicenseKey(license);
        }
        catch (IOException e)
        {
            addActionError(getText("license.key.update.failed", e.getMessage()));
            return ERROR;
        }
        return SUCCESS;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
