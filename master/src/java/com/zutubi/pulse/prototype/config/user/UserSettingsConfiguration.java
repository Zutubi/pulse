package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.model.BuildColumns;
import com.zutubi.pulse.web.DefaultAction;
import com.zutubi.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@SymbolicName("internal.userSettingConfig")
public class UserSettingsConfiguration extends AbstractConfiguration
{
    private static final String PROPERTY_DASHBOARD_BUILD_COUNT = "user.dashboardBuildCount";
    private static final String PROPERTY_DEFAULT_ACTION = "user.defaultAction";
    private static final String PROPERTY_SHOW_MY_CHANGES = "show.my.changes";
    private static final String PROPERTY_MY_CHANGES_COUNT = "my.changes.count";
    private static final String PROPERTY_SHOW_PROJECT_CHANGES = "show.project.changes";
    private static final String PROPERTY_PROJECT_CHANGES_COUNT = "project.changes.count";
    private static final String PROPERTY_REFRESH_INTERVAL = "user.refreshInterval";
    private static final String PROPERTY_TAIL_LINES = "tail.lines";
    private static final String PROPERTY_TAIL_REFRESH_INTERVAL = "tail.refresh.interval";
    private static final String PROPERTY_MY_BUILDS_COUNT = "my.builds.count";
    private static final String PROPERTY_SHOW_ALL_PROJECTS = "show.all.projects";
    private static final String PROPERTY_MY_BUILDS_COLUMNS = "my.builds.columns";
    private static final String PROPERTY_MY_PROJECTS_COLUMNS = "my.projects.columns";
    private static final String PROPERTY_ALL_PROJECTS_COLUMNS = "all.projects.columns";
    private static final String PROPERTY_PROJECT_SUMMARY_COLUMNS = "project.summary.columns";
    private static final String PROPERTY_PROJECT_RECENT_COLUMNS = "project.recent.columns";
    private static final String PROPERTY_PROJECT_HISTORY_COLUMNS = "project.history.columns";

    @Transient // FIXME.
    private Map<String, String> properties = new HashMap<String, String>();

    @Select(optionProvider = "DefaultActionOptionProvider")
    public String getDefaultAction()
    {
        if (hasProperty(PROPERTY_DEFAULT_ACTION))
        {
            return getProperty(PROPERTY_DEFAULT_ACTION);
        }
        return DefaultAction.DASHBOARD_ACTION;
    }

    public void setDefaultAction(String defaultAction)
    {
        setProperty(PROPERTY_DEFAULT_ACTION, defaultAction);
    }

    /**
     * Number of seconds between refreshes of "live" content, or 0 if the
     * user disables refreshing.
     */
    public int getRefreshInterval()
    {
        if (hasProperty(PROPERTY_REFRESH_INTERVAL))
        {
            return Integer.valueOf(getProperty(PROPERTY_REFRESH_INTERVAL));
        }
        return 60;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        setProperty(PROPERTY_REFRESH_INTERVAL, Integer.toString(refreshInterval));
    }

    public int getTailRefreshInterval()
    {
        return getIntProperty(PROPERTY_TAIL_REFRESH_INTERVAL, 60);
    }

    public void setTailRefreshInterval(int interval)
    {
        setIntProperty(PROPERTY_TAIL_REFRESH_INTERVAL, interval);
    }

    public int getTailLines()
    {
        return getIntProperty(PROPERTY_TAIL_LINES, 30);
    }

    public void setTailLines(int lines)
    {
        setIntProperty(PROPERTY_TAIL_LINES, lines);
    }

    @Internal
    public int getDashboardBuildCount()
    {
        if (hasProperty(PROPERTY_DASHBOARD_BUILD_COUNT))
        {
            return Integer.valueOf(getProperty(PROPERTY_DASHBOARD_BUILD_COUNT));
        }
        return 2;
    }

    public void setDashboardBuildCount(int buildCount)
    {
        setProperty(PROPERTY_DASHBOARD_BUILD_COUNT, Integer.toString(buildCount));
    }

    @Internal
    public boolean getShowAllProjects()
    {
        return getBooleanProperty(PROPERTY_SHOW_ALL_PROJECTS, true);
    }

    public void setShowAllProjects(boolean show)
    {
        setBooleanProperty(PROPERTY_SHOW_ALL_PROJECTS, show);
    }

    @Internal
    public boolean getShowMyChanges()
    {
        return getBooleanProperty(PROPERTY_SHOW_MY_CHANGES, true);
    }

    public void setShowMyChanges(boolean show)
    {
        setBooleanProperty(PROPERTY_SHOW_MY_CHANGES, show);
    }

    @Internal
    public int getMyChangesCount()
    {
        return getIntProperty(PROPERTY_MY_CHANGES_COUNT, 10);
    }

    public void setMyChangesCount(int count)
    {
        setIntProperty(PROPERTY_MY_CHANGES_COUNT, count);
    }

    @Internal
    public boolean getShowProjectChanges()
    {
        return getBooleanProperty(PROPERTY_SHOW_PROJECT_CHANGES, false);
    }

    public void setShowProjectChanges(boolean show)
    {
        setBooleanProperty(PROPERTY_SHOW_PROJECT_CHANGES, show);
    }

    @Internal
    public int getProjectChangesCount()
    {
        return getIntProperty(PROPERTY_PROJECT_CHANGES_COUNT, 10);
    }

    public void setProjectChangesCount(int count)
    {
        setIntProperty(PROPERTY_PROJECT_CHANGES_COUNT, count);
    }

    public int getMyBuildsCount()
    {
        return getIntProperty(PROPERTY_MY_BUILDS_COUNT, 5);
    }

    public void setMyBuildsCount(int count)
    {
        setIntProperty(PROPERTY_MY_BUILDS_COUNT, count);
    }

    @Internal
    public String getMyBuildsColumns()
    {
        return getStringProperty(PROPERTY_MY_BUILDS_COLUMNS, StringUtils.join(",", BuildColumns.KEY_ID, BuildColumns.KEY_PROJECT, BuildColumns.KEY_STATUS, BuildColumns.KEY_TESTS, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_ACTIONS));
    }

    public void setMyBuildsColumns(String columns)
    {
        setProperty(PROPERTY_MY_BUILDS_COLUMNS, columns);
    }

    @Internal
    public String getMyProjectsColumns()
    {
        return getStringProperty(PROPERTY_MY_PROJECTS_COLUMNS, getDefaultProjectColumns());
    }

    public void setMyProjectsColumns(String columns)
    {
        setProperty(PROPERTY_MY_PROJECTS_COLUMNS, columns);
    }

    @Internal
    public String getAllProjectsColumns()
    {
        return getStringProperty(PROPERTY_ALL_PROJECTS_COLUMNS, getDefaultAllProjectsColumns());
    }

    public void setAllProjectsColumns(String columns)
    {
        setProperty(PROPERTY_ALL_PROJECTS_COLUMNS, columns);
    }

    @Internal
    public String getProjectSummaryColumns()
    {
        return getStringProperty(PROPERTY_PROJECT_SUMMARY_COLUMNS, getDefaultProjectColumns());
    }

    public void setProjectSummaryColumns(String columns)
    {
        setProperty(PROPERTY_PROJECT_SUMMARY_COLUMNS, columns);
    }

    @Internal
    public String getProjectRecentColumns()
    {
        return getStringProperty(PROPERTY_PROJECT_RECENT_COLUMNS, getDefaultProjectColumns());
    }

    public void setProjectRecentColumns(String columns)
    {
        setProperty(PROPERTY_PROJECT_RECENT_COLUMNS, columns);
    }

    @Internal
    public String getProjectHistoryColumns()
    {
        return getStringProperty(PROPERTY_PROJECT_HISTORY_COLUMNS, getDefaultProjectColumns());
    }

    public void setProjectHistoryColumns(String columns)
    {
        setProperty(PROPERTY_PROJECT_HISTORY_COLUMNS, columns);
    }

    @Internal
    public static String getDefaultProjectColumns()
    {
        return StringUtils.join(",", BuildColumns.KEY_ID, BuildColumns.KEY_STATUS, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_ACTIONS);
    }

    public static String getDefaultAllProjectsColumns()
    {
        return StringUtils.join(",", BuildColumns.KEY_ID, BuildColumns.KEY_STATUS, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_ACTIONS);
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private boolean getBooleanProperty(String property, boolean defaultValue)
    {
        if(hasProperty(property))
        {
            return Boolean.valueOf(getProperty(property));
        }

        return defaultValue;
    }

    private void setBooleanProperty(String property, boolean value)
    {
        setProperty(property, Boolean.toString(value));
    }

    private int getIntProperty(String property, int defaultValue)
    {
        if(hasProperty(property))
        {
            return Integer.parseInt(getProperty(property));
        }

        return defaultValue;
    }

    private void setIntProperty(String property, int value)
    {
        setProperty(property, Integer.toString(value));
    }

    private String getStringProperty(String property, String defaultValue)
    {
        if(hasProperty(property))
        {
            return getProperty(property);
        }

        return defaultValue;
    }

    private void setProperty(String key, String value)
    {
        getProperties().put(key, value);
    }

    private String getProperty(String key)
    {
        return getProperties().get(key);
    }

    private boolean hasProperty(String key)
    {
        return getProperties().containsKey(key);
    }


}
