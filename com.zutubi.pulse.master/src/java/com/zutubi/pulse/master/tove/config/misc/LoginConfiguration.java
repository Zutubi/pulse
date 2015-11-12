package com.zutubi.pulse.master.tove.config.misc;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Password;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Text;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Transient configuration used for the login form.  The odd field names are
 * to match Acegi expectations.
 */
@Form(fieldOrder = { "username", "password", "_spring_security_remember_me" }, actions = { "login" })
@SymbolicName("zutubi.transient.login")
public class LoginConfiguration extends AbstractConfiguration
{
    @Text(size = 200) @Required
    private String username;
    @Password(size = 200)
    private String password;
    private boolean rememberMe;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isRememberMe()
    {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe)
    {
        this.rememberMe = rememberMe;
    }
}
