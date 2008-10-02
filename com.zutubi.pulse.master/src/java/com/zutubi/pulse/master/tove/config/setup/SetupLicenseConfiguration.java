package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.TextArea;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseDecoder;
import com.zutubi.pulse.master.license.LicenseException;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * Used for the setup license page.
 */
@SymbolicName("zutubi.setupLicenseConfig")
@Form(fieldOrder = { "license" }, actions = { "next" })
public class SetupLicenseConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    @TextArea(rows = 10, cols = 80)
    private String license;

    public String getLicense()
    {
        return license;
    }

    public void setLicense(String license)
    {
        this.license = license;
    }

    public void validate(ValidationContext context)
    {
        // take the license string, strip out any '\n' chars and check it.
        String licenseKey = license.replaceAll("\n", "");
        LicenseDecoder decoder = new LicenseDecoder();
        try
        {
            License l = decoder.decode(licenseKey.getBytes());
            if (l == null)
            {
                context.addFieldError("license", "license.key.invalid");
            }
            else if (l.isExpired() && l.isEvaluation())
            {
                context.addFieldError("license", "license.key.expired");
            }
            else if(!l.canRunVersion(Version.getVersion()))
            {
                context.addFieldError("license", "license.key.cannot.run");
            }
        }
        catch (LicenseException e)
        {
            context.addFieldError("license", "license.decode.error");
        }
    }
}
