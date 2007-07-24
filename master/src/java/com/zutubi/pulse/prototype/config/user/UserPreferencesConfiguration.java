package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Nested composite to store user preferences under their own path.
 */
@SymbolicName("zutubi.userPreferencesConfig")
public class UserPreferencesConfiguration extends AbstractConfiguration
{
    private UserSettingsConfiguration settings = new UserSettingsConfiguration();
    private List<UserAliasConfiguration> alias = new LinkedList<UserAliasConfiguration>();
    private Map<String, ContactConfiguration> contacts = new HashMap<String, ContactConfiguration>();
    private Map<String, SubscriptionConfiguration> subscriptions = new HashMap<String, SubscriptionConfiguration>();
    private DashboardConfiguration dashboard = new DashboardConfiguration();

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

    public Map<String, SubscriptionConfiguration> getSubscriptions()
    {
        return subscriptions;
    }

    public void setSubscriptions(Map<String, SubscriptionConfiguration> subscriptions)
    {
        this.subscriptions = subscriptions;
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
