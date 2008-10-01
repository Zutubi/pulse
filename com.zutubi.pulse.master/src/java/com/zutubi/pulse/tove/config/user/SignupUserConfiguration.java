package com.zutubi.pulse.tove.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * Transient configuration that allows a user to be created via self-signup.
 */
@SymbolicName("zutubi.signupUserConfig")
@Form(fieldOrder = {"login", "name", "password", "confirmPassword"})
public class SignupUserConfiguration extends AbstractConfiguration implements Validateable
{
    @Required
    private String login;
    @Required
    private String name;
    @Password
    private String password;
    @Password
    private String confirmPassword;

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
        UserManager userManager = SpringComponentContext.getBean("userManager");
        if(login != null && userManager.getUser(login) != null)
        {
            context.addFieldError("login", "login '" + login + "' is already in use; please choose another login");
        }
        
        if(password != null && confirmPassword != null)
        {
            if(!password.equals(confirmPassword))
            {
                context.addFieldError("password", "passwords do not match");
            }
        }
    }
}
