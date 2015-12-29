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
@Form(fieldOrder = { "name", "email" })
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
