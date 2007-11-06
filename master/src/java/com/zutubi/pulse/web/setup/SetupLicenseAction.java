package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseManager;

/**
 * <class-comment/>
 */
public class SetupLicenseAction extends SetupActionSupport
{
    private LicenseManager licenseManager;

    private String license;

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public void validate()
    {
        // take the license string, strip out any '\n' chars and check it.
        String licenseKey = license.replaceAll("\n", "");
        LicenseDecoder decoder = new LicenseDecoder();
        try
        {
            License l = decoder.decode(licenseKey.getBytes());
            if (l == null)
            {
                addFieldError("license", getText("license.key.invalid"));
            }
        }
        catch (LicenseException e)
        {
            addFieldError("license", getText("license.decode.error"));
        }
    }

    public String execute() throws Exception
    {
        String licenseKey = license.replaceAll("\n", "");
        licenseManager.installLicense(licenseKey);
        setupManager.requestLicenseComplete();
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
}
