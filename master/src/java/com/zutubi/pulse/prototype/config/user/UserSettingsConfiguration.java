package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.model.BuildColumns;
import com.zutubi.pulse.web.DefaultAction;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Numeric;

/**
 * General user preferences that do not fit in another category.
 */
@SymbolicName("zutubi.userSettingConfig")
@Form(labelWidth = 250, fieldOrder = {"defaultAction", "myBuildsCount", "refreshingEnabled", "refreshInterval", "tailLines", "tailRefreshInterval"})
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


    @Select(optionProvider = "DefaultActionOptionProvider")
    private String defaultAction = DefaultAction.WELCOME_ACTION;
    @ControllingCheckbox(dependentFields = "refreshInterval")
    boolean refreshingEnabled = true;
    /**
     * Number of seconds between refreshes of "live" content, or 0 if the
     * user disables refreshing.
     */
    @Numeric(min = 1)
    private int refreshInterval = 60;
    @Numeric(min = 1)
    private int tailRefreshInterval = 60;
    @Numeric(min = 1)
    private int tailLines = 30;
    @Numeric(min = 1)
    private int myBuildsCount = 5;

    @Internal
    private String myBuildsColumns = defaultProjectColumns();
    @Internal
    private String myProjectsColumns = defaultProjectColumns();
    @Internal
    private String allProjectsColumns = defaultAllProjectsColumns();
    @Internal
    private String projectSummaryColumns = defaultProjectColumns();
    @Internal
    private String projectRecentColumns = defaultProjectColumns();
    @Internal
    private String projectHistoryColumns = defaultProjectColumns();

    public String getDefaultAction()
    {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction)
    {
        this.defaultAction = defaultAction;
    }

    public boolean isRefreshingEnabled()
    {
        return refreshingEnabled;
    }

    public void setRefreshingEnabled(boolean refreshingEnabled)
    {
        this.refreshingEnabled = refreshingEnabled;
    }

    public int getRefreshInterval()
    {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval)
    {
        this.refreshInterval = refreshInterval;
    }

    public int getTailRefreshInterval()
    {
        return tailRefreshInterval;
    }

    public void setTailRefreshInterval(int tailRefreshInterval)
    {
        this.tailRefreshInterval = tailRefreshInterval;
    }

    public int getTailLines()
    {
        return tailLines;
    }

    public void setTailLines(int tailLines)
    {
        this.tailLines = tailLines;
    }

    public int getMyBuildsCount()
    {
        return myBuildsCount;
    }

    public void setMyBuildsCount(int myBuildsCount)
    {
        this.myBuildsCount = myBuildsCount;
    }

    public String getMyBuildsColumns()
    {
        return myBuildsColumns;
    }

    public void setMyBuildsColumns(String myBuildsColumns)
    {
        this.myBuildsColumns = myBuildsColumns;
    }

    public String getMyProjectsColumns()
    {
        return myProjectsColumns;
    }

    public void setMyProjectsColumns(String myProjectsColumns)
    {
        this.myProjectsColumns = myProjectsColumns;
    }

    public String getAllProjectsColumns()
    {
        return allProjectsColumns;
    }

    public void setAllProjectsColumns(String allProjectsColumns)
    {
        this.allProjectsColumns = allProjectsColumns;
    }

    public String getProjectSummaryColumns()
    {
        return projectSummaryColumns;
    }

    public void setProjectSummaryColumns(String projectSummaryColumns)
    {
        this.projectSummaryColumns = projectSummaryColumns;
    }

    public String getProjectRecentColumns()
    {
        return projectRecentColumns;
    }

    public void setProjectRecentColumns(String projectRecentColumns)
    {
        this.projectRecentColumns = projectRecentColumns;
    }

    public String getProjectHistoryColumns()
    {
        return projectHistoryColumns;
    }

    public void setProjectHistoryColumns(String projectHistoryColumns)
    {
        this.projectHistoryColumns = projectHistoryColumns;
    }

    public static String defaultProjectColumns()
    {
        return StringUtils.join(",", BuildColumns.KEY_ID, BuildColumns.KEY_STATUS, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_ACTIONS);
    }

    public static String defaultAllProjectsColumns()
    {
        return StringUtils.join(",", BuildColumns.KEY_ID, BuildColumns.KEY_STATUS, BuildColumns.KEY_REASON, BuildColumns.KEY_TESTS, BuildColumns.KEY_WHEN, BuildColumns.KEY_ELAPSED, BuildColumns.KEY_ACTIONS);
    }
}
