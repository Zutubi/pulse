package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.GrantedAuthority;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.type.Extendable;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a user of Pulse, including account details and the
 * user's personal preferences.
 */
@SymbolicName("zutubi.userConfig")
@Form(fieldOrder = {"login", "name", "authenticatedViaLdap"})
@Table(columns = {"login", "name"})
@Classification(collection = "users")
public class UserConfiguration extends AbstractConfiguration implements Extendable
{
    @ID
    private String login;
    @Required
    private String name;
    @Internal
    private String password;
    private boolean authenticatedViaLdap;
    @Internal
    private List<String> directAuthorities;
    private UserPreferencesConfiguration preferences = new UserPreferencesConfiguration();

    @Transient
    private Map<String, Object> extensions;

    @ExternalState
    private long userId;

    public UserConfiguration()
    {
        directAuthorities = new LinkedList<String>();
        directAuthorities.add(GrantedAuthority.USER);
    }

    public UserConfiguration(String login, String name)
    {
        this();
        this.login = login;
        this.name = name;
    }

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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isAuthenticatedViaLdap()
    {
        return authenticatedViaLdap;
    }

    public void setAuthenticatedViaLdap(boolean authenticatedViaLdap)
    {
        this.authenticatedViaLdap = authenticatedViaLdap;
    }

    public List<String> getDirectAuthorities()
    {
        return directAuthorities;
    }

    public void setDirectAuthorities(List<String> directAuthorities)
    {
        this.directAuthorities = directAuthorities;
    }

    public void addDirectAuthority(String authority)
    {
        directAuthorities.add(authority);
    }

    @Transient
    public String getDefaultAuthority()
    {
        return "user:" + login;
    }

    @Transient
    public String[] getGrantedAuthorities()
    {
        String[] result = new String[directAuthorities.size() + 1];
        directAuthorities.toArray(result);
        result[result.length - 1] = getDefaultAuthority();
        return result;
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

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }
}
