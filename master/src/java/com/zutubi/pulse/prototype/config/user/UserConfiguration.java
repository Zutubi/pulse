package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.prototype.type.Extendable;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
@SymbolicName("zutubi.userConfig")
@Form(fieldOrder = {"login", "name"})
public class UserConfiguration extends AbstractConfiguration implements Extendable
{
    @Transient
    private Map<String, Object> extensions;

    @ID
    private String login;

    private String name;

    private UserSettingsConfiguration settings = new UserSettingsConfiguration();

    private List<UserAliasConfiguration> alias = new LinkedList<UserAliasConfiguration>();

    private Map<String, ContactConfiguration> contacts = new HashMap<String, ContactConfiguration>();

    private List<SubscriptionConfiguration> subscriptions = new LinkedList<SubscriptionConfiguration>();

    private DashboardConfiguration dashboard = new DashboardConfiguration();

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

    public UserSettingsConfiguration getSettings()
    {
        return settings;
    }

    public void setSettings(UserSettingsConfiguration settings)
    {
        this.settings = settings;
    }

    public List<UserAliasConfiguration> getAlias()
    {
        return alias;
    }

    public void setAlias(List<UserAliasConfiguration> alias)
    {
        this.alias = alias;
    }

    public Map<String, ContactConfiguration> getContacts()
    {
        return contacts;
    }

    public void setContacts(Map<String, ContactConfiguration> contacts)
    {
        this.contacts = contacts;
    }

    public List<SubscriptionConfiguration> getSubscriptions()
    {
        return subscriptions;
    }

    public void setSubscriptions(List<SubscriptionConfiguration> subscriptions)
    {
        this.subscriptions = subscriptions;
    }

    public Map<String, Object> getExtensions()
    {
        if (extensions == null)
        {
            extensions = new HashMap<String, Object>();
        }
        return extensions;
    }

    public DashboardConfiguration getDashboard()
    {
        return dashboard;
    }

    public void setDashboard(DashboardConfiguration dashboard)
    {
        this.dashboard = dashboard;
    }
}
