package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * Transient configuration that allows a user to be created via self-signup.
 */
@SymbolicName("zutubi.signupUserConfig")
@Form(fieldOrder = {"login", "name", "password", "confirmPassword"})
@Wire
public class SignupUserConfiguration extends AbstractConfiguration implements Validateable
{
    private static final Messages I18N = Messages.getInstance(SignupUserConfiguration.class);

    @Required
    private String login;
    @Required
    private String name;
    @Password
    private String password;
    @Password
    private String confirmPassword;

    @Transient
    private UserManager userManager;

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

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }

    public void validate(ValidationContext context)
    {
        if(login != null && userManager.getUser(login) != null)
        {
            context.addFieldError("login", "login '" + login + "' is already in use; please choose another login");
        }
        
        if(password != null && confirmPassword != null)
        {
            if(!password.equals(confirmPassword))
            {
                context.addFieldError("password", I18N.format("passwords.differ"));
            }
        }
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
