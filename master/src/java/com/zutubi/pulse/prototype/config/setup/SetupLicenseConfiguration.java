package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.TextArea;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * Used for the setup license page.
 */
@SymbolicName("internal.setupLicenseConfig")
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
                return;
            }
            if (l.isExpired())
            {
                context.addFieldError("license", "license.key.expired");
            }
        }
        catch (LicenseException e)
        {
            context.addFieldError("license", "license.decode.error");
        }
    }
}
