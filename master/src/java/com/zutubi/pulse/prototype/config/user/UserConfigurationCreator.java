package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.ConfigurationCreator;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.annotations.Required;

/**
 * Used when creating new users.  Handles the confirmation of the user's
 * password.
 */
@SymbolicName("zutubi.userConfigCreator")
public class UserConfigurationCreator extends AbstractConfiguration implements ConfigurationCreator<UserConfiguration>, Validateable
{
    @ID
    private String login;
    @Required
    private String name;
    @ControllingCheckbox(dependentFields = {"password", "confirmPassword"}, invert = true)
    private boolean authenticatedViaLdap;
    @Password
    private String password;
    @Password
    private String confirmPassword;

    public UserConfiguration create()
    {
        UserConfiguration user = new UserConfiguration(login, name);
        if(authenticatedViaLdap)
        {
            user.setAuthenticatedViaLdap(true);
        }
        else
        {
            // Hashing is handled at a lower level
            user.setPassword(password);
        }

        return user;
    }

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

    public boolean isAuthenticatedViaLdap()
    {
        return authenticatedViaLdap;
    }

    public void setAuthenticatedViaLdap(boolean authenticatedViaLdap)
    {
        this.authenticatedViaLdap = authenticatedViaLdap;
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
        if(!authenticatedViaLdap)
        {
            if(password != null && confirmPassword != null)
            {
                if(!password.equals(confirmPassword))
                {
                    context.addFieldError("password", "passwords do not match");
                }
            }
        }
    }
}
