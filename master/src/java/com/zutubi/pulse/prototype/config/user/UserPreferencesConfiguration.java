package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Classification;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Nested composite to store user preferences under their own path.
 */
@SymbolicName("zutubi.userPreferencesConfig")
@Classification(single = "settings")
public class UserPreferencesConfiguration extends AbstractConfiguration
{
    private UserSettingsConfiguration settings = new UserSettingsConfiguration();
    private Map<String, ContactConfiguration> contacts = new HashMap<String, ContactConfiguration>();
    private Map<String, SubscriptionConfiguration> subscriptions = new HashMap<String, SubscriptionConfiguration>();
    private DashboardConfiguration dashboard = new DashboardConfiguration();

    public UserPreferencesConfiguration()
    {
        settings.setPermanent(true);
        dashboard.setPermanent(true);
    }

    public UserSettingsConfiguration getSettings()
    {
        return settings;
    }

    public void setSettings(UserSettingsConfiguration settings)
    {
        this.settings = settings;
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
