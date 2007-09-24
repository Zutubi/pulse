package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Password;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * Transient configuration type used to change a user's password.
 */
@SymbolicName("zutubi.setPasswordConfig")
@Form(fieldOrder = {"password", "confirmPassword"})
public class SetPasswordConfiguration extends AbstractConfiguration implements Validateable
{
    @Password
    private String password;
    @Password
    private String confirmPassword;

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
        if(password != null && confirmPassword != null)
        {
            if(!password.equals(confirmPassword))
            {
                context.addFieldError("password", "passwords do not match");
            }
        }
    }
}
