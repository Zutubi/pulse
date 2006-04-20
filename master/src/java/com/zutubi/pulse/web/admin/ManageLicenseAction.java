package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Home;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseUpdateEvent;
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

    private EventManager eventManager;

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

    public void validate()
    {
        try
        {
            String licenseKey = license.replaceAll("\n", "");
            License l = new LicenseDecoder().decode(licenseKey.getBytes());
            if (l.hasExpired())
            {
                addFieldError("license", getText("license.key.expired"));
            }
        }
        catch (LicenseException e)
        {
            addActionError(getText("license.key.valdiation.error", e.getMessage()));
        }
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
            addActionError(getText("license.key.update.error", e.getMessage()));
            return ERROR;
        }

        // todo: move this into the business logic layer.. will need to move the updating license
        // todo: into a system license store / manager object..
        eventManager.publish(new LicenseUpdateEvent(home.getLicense()));
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

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
