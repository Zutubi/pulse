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
@Form(fieldOrder = { "j_username", "j_password", "_acegi_security_remember_me" }, actions = { "login" })
@SymbolicName("zutubi.transient.login")
public class LoginConfiguration extends AbstractConfiguration
{
    @Text(size = 200) @Required
    private String j_username;
    @Password(size = 200)
    private String j_password;
    private boolean _acegi_security_remember_me;

    public String getJ_username()
    {
        return j_username;
    }

    public void setJ_username(String j_username)
    {
        this.j_username = j_username;
    }

    public String getJ_password()
    {
        return j_password;
    }

    public void setJ_password(String j_password)
    {
        this.j_password = j_password;
    }

    public boolean is_acegi_security_remember_me()
    {
        return _acegi_security_remember_me;
    }

    public void set_acegi_security_remember_me(boolean _acegi_security_remember_me)
    {
        this._acegi_security_remember_me = _acegi_security_remember_me;
    }
}
