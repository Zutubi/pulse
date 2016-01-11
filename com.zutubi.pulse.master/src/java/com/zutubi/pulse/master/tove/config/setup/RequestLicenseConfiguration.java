package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

/**
 * Used for the AJAX request license form on the setup license page.
 */
@SymbolicName("zutubi.requestLicenseConfig")
@Form(fieldOrder = { "fullName", "email" })
public class RequestLicenseConfiguration extends AbstractConfiguration
{
    @Required
    private String fullName;
    @Required
    @Email
    private String email;

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
