package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

/**
 * Used for the AJAX request license form on the setup license page.
 */
@SymbolicName("internal.requestLicenseConfig")
@Form(fieldOrder = { "name", "email" }, actions = { "request" })
public class RequestLicenseConfiguration extends AbstractConfiguration
{
    @Required
    private String name;
    @Required
    @Email
    private String email;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
