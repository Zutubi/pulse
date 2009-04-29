package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the state for a user account.
 */
public class User extends Entity
{
    public static int REFRESH_DISABLED = 0;

    /**
     * Indicates whether or not the user is enabled. Only enabled users can
     * log in to the system.
     */
    private boolean enabled = true;
    /**
     * Number to use for this user's next personal build.
     */
    private long nextBuildNumber = 1;
    /**
     * Collapsed groups/projects on the browse view.
     */
    private Set<LabelProjectTuple> browseViewCollapsed = new HashSet<LabelProjectTuple>();
    /**
     * Collapsed groups/projects on the dashboard.
     */
    private Set<LabelProjectTuple> dashboardCollapsed = new HashSet<LabelProjectTuple>();

    private UserConfiguration config;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public long getNextBuildNumber()
    {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(long nextBuildNumber)
    {
        this.nextBuildNumber = nextBuildNumber;
    }

    public UserConfiguration getConfig()
    {
        return config;
    }

    public String getName()
    {
        return config != null ? config.getName() : null;
    }

    public String getLogin()
    {
        return config != null ? config.getLogin() : null;
    }

    public void setConfig(UserConfiguration config)
    {
        this.config = config;
    }

    public UserPreferencesConfiguration getPreferences()
    {
        if (config == null)
        {
            return null;
        }
        else
        {
            return config.getPreferences();
        }
    }

    public Set<LabelProjectTuple> getBrowseViewCollapsed()
    {
        return browseViewCollapsed;
    }

    public void setBrowseViewCollapsed(Set<LabelProjectTuple> browseViewCollapsed)
    {
        this.browseViewCollapsed = browseViewCollapsed;
    }

    public Set<LabelProjectTuple> getDashboardCollapsed()
    {
        return dashboardCollapsed;
    }

    public void setDashboardCollapsed(Set<LabelProjectTuple> dashboardCollapsed)
    {
        this.dashboardCollapsed = dashboardCollapsed;
    }
}
