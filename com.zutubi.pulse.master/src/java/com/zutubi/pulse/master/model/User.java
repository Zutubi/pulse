/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the state for a user account.
 */
public class User extends Entity implements NamedEntity
{
    public static final String DEFAULT_ARTIFACTS_FILTER = "explicit";

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
     * The last time the user access the web interface.  These numbers are not
     * flushed to the database on every access (for performance reasons).
     */
    private long lastAccessTime = 0;
    /**
     * Project state filter for the browse view.
     */
    private String browseViewFilter = "";
    /**
     * Collapsed groups/projects on the browse view.
     */
    private Set<LabelProjectTuple> browseViewCollapsed = new HashSet<LabelProjectTuple>();
    /**
     * Project state filter for the dashboard view.
     */
    private String dashboardFilter = "";
    /**
     * Collapsed groups/projects on the dashboard.
     */
    private Set<LabelProjectTuple> dashboardCollapsed = new HashSet<LabelProjectTuple>();
    /**
     * Name of the property to filter artifacts by, empty for none.
     */
    private String artifactsFilter = DEFAULT_ARTIFACTS_FILTER;

    private UserConfiguration config;

    public User()
    {
    }

    public User(UserConfiguration config)
    {
        this.config = config;
    }

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

    public long getLastAccessTime()
    {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime)
    {
        this.lastAccessTime = lastAccessTime;
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

    public String getBrowseViewFilter()
    {
        return browseViewFilter;
    }

    public void setBrowseViewFilter(String browseViewFilter)
    {
        this.browseViewFilter = browseViewFilter;
    }

    public Set<LabelProjectTuple> getBrowseViewCollapsed()
    {
        return browseViewCollapsed;
    }

    public void setBrowseViewCollapsed(Set<LabelProjectTuple> browseViewCollapsed)
    {
        this.browseViewCollapsed = browseViewCollapsed;
    }

    public String getDashboardFilter()
    {
        return dashboardFilter;
    }

    public void setDashboardFilter(String dashboardFilter)
    {
        this.dashboardFilter = dashboardFilter;
    }

    public Set<LabelProjectTuple> getDashboardCollapsed()
    {
        return dashboardCollapsed;
    }

    public void setDashboardCollapsed(Set<LabelProjectTuple> dashboardCollapsed)
    {
        this.dashboardCollapsed = dashboardCollapsed;
    }

    public String getArtifactsFilter()
    {
        return artifactsFilter;
    }

    public void setArtifactsFilter(String artifactsFilter)
    {
        this.artifactsFilter = artifactsFilter;
    }
}
