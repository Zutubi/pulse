package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.prototype.config.user.UserPreferencesConfiguration;

/**
 * 
 *
 */
public class User extends Entity
{
    public static int REFRESH_DISABLED = 0;

    public static final String PROPERTY_DASHBOARD_BUILD_COUNT = "user.dashboardBuildCount";
    public static final String PROPERTY_DEFAULT_ACTION = "user.defaultAction";
    public static final String PROPERTY_LDAP_AUTHENTICATION = "user.ldapAuthentication";
    public static final String PROPERTY_SHOW_MY_CHANGES = "show.my.changes";
    public static final String PROPERTY_MY_CHANGES_COUNT = "my.changes.count";
    public static final String PROPERTY_SHOW_PROJECT_CHANGES = "show.project.changes";
    public static final String PROPERTY_PROJECT_CHANGES_COUNT = "project.changes.count";
    public static final String PROPERTY_REFRESH_INTERVAL = "user.refreshInterval";
    public static final String PROPERTY_TAIL_LINES = "tail.lines";
    public static final String PROPERTY_TAIL_REFRESH_INTERVAL = "tail.refresh.interval";
    public static final String PROPERTY_MY_BUILDS_COUNT = "my.builds.count";
    public static final String PROPERTY_SHOW_ALL_PROJECTS = "show.all.projects";
    public static final String PROPERTY_MY_BUILDS_COLUMNS = "my.builds.columns";
    public static final String PROPERTY_MY_PROJECTS_COLUMNS = "my.projects.columns";
    public static final String PROPERTY_ALL_PROJECTS_COLUMNS = "all.projects.columns";
    public static final String PROPERTY_PROJECT_SUMMARY_COLUMNS = "project.summary.columns";
    public static final String PROPERTY_PROJECT_RECENT_COLUMNS = "project.recent.columns";
    public static final String PROPERTY_PROJECT_HISTORY_COLUMNS = "project.history.columns";

    /**
     * Indicates whether or not the user is enabled. Only enabled users can
     * log in to the system.
     */
    private boolean enabled = true;
    private long nextBuildNumber = 1;
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
}
