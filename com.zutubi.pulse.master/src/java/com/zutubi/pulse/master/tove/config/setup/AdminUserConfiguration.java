package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

/**
 * Used to configure the admin user during the setup wizard.
 */
@SymbolicName("zutubi.adminUserConfig")
@Form(fieldOrder = {"login", "name", "emailAddress", "password", "confirm"})
public class AdminUserConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    private String login;
    @Required
    private String name;
    @Email
    private String emailAddress;
    @Password(showPassword = true)
    private String password;
    @Password(showPassword = true)
    private String confirm;

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public void validate(ValidationContext context)
    {
        if (!confirm.equals(getPassword()))
        {
            context.addFieldError("confirm", context.getTextProvider(this).getText("admin.password.confirm.mismatch"));
        }
    }
}
