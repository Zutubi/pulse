package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 *
 *
 */
@SymbolicName("internal.adminUserConfig")
@Form(fieldOrder = {"login", "name", "password", "confirm"})
public class AdminUserConfiguration implements Validateable
{
    @Required
    private String login;

    @Required
    private String name;
    
    @Password
    private String password;
    
    @Password
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
