package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.prototype.type.Extendable;
import com.zutubi.pulse.core.config.AbstractConfiguration;

import java.util.Map;

/**
 *
 *
 */
@SymbolicName("zutubi.userConfig")
@Form(fieldOrder = {"login", "name"})
public class UserConfiguration extends AbstractConfiguration implements Extendable
{
    @ID
    private String login;
    private String name;
    private String password;
    private UserPreferencesConfiguration preferences = new UserPreferencesConfiguration();

    @Transient
    private Map<String, Object> extensions;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public UserPreferencesConfiguration getPreferences()
    {
        return preferences;
    }

    public void setPreferences(UserPreferencesConfiguration preferences)
    {
        this.preferences = preferences;
    }

    public Map<String, Object> getExtensions()
    {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions)
    {
        this.extensions = extensions;
    }
}
