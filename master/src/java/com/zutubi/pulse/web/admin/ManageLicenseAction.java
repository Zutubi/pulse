package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.license.*;
import com.zutubi.pulse.web.ActionSupport;

/**
 * The manage license action supports updating the license currently installed
 * in this system.
 *
 * @author Daniel Ostermeier
 */
public class ManageLicenseAction extends ActionSupport
{
    private LicenseManager licenseManager;

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
        if (hasErrors())
        {
            return;
        }

        try
        {
            String licenseKey = license.replaceAll("\n", "");
            License l = new LicenseDecoder().decode(licenseKey.getBytes());
            if (l.isExpired())
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
        try
        {
            licenseManager.updateLicenseKey(license);
        }
        catch (LicenseException e)
        {
            addActionError(getText("license.key.update.error", e.getMessage()));
            return ERROR;
        }

        // todo: move this into the business logic layer.. will need to move the updating license
        // todo: into a system license store / manager object..
        eventManager.publish(new LicenseUpdateEvent(LicenseHolder.getLicense()));
        return SUCCESS;
    }

    /**
     * Required resource.
     *
     * @param licenseManager
     */
    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
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
