package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.LinkTable;
import com.zutubi.pulse.acceptance.components.PropertyTable;
import com.zutubi.pulse.acceptance.components.StatusBox;
import com.zutubi.pulse.acceptance.components.SummaryTable;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
import static com.zutubi.util.WebUtils.uriComponentEncode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends ResponsibilityPage
{
    private static final String PROPERTY_HEALTH = "health";
    private static final String PROPERTY_STATE = "state";
    private static final String PROPERTY_SUCCESS_RATE = "successRate";
    private static final String PROPERTY_STATISTICS = "statistics";

    private static final String COLUMN_ACTIVITY_STATUS = "status";

    private static final String ID_LATEST_NUMBER = "number";
    private static final String ID_LATEST_STATUS = "status";

    private String projectName;
    private StatusBox statusBox;
    private SummaryTable activityTable;
    private PropertyTable latestCompleteTable;
    private SummaryTable recentTable;
    private SummaryTable changesTable;
    private LinkTable actionsTable;
    private LinkTable linksTable;

    public ProjectHomePage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-home-" + projectName, projectName);
        this.projectName = projectName;
        statusBox = new StatusBox(browser, "project-status");
        activityTable = new SummaryTable(browser, "project-activity");
        latestCompleteTable = new PropertyTable(browser, "project-latest");
        recentTable = new SummaryTable(browser, "project-recent");
        changesTable = new SummaryTable(browser, "project-changes");
        actionsTable = new LinkTable(browser, "project-actions");
        linksTable = new LinkTable(browser, "project-links");
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("view.initialised");
    }

    /**
     * Returns the project's displayed health.
     *
     * @return reported health of the project
     */
    public String getHealth()
    {
        return statusBox.getValue(PROPERTY_HEALTH);
    }

    /**
     * Returns the project's displayed state.
     *
     * @return reported state of the project
     */
    public String getState()
    {
        String state = statusBox.getValue(PROPERTY_STATE).trim();
        // Remove trailing actions.
        state = StringUtils.stripSuffix(state, "pause");
        state = StringUtils.stripSuffix(state, "resume");
        state = state.trim();
        return state;
    }

    /**
     * Returns the project's success rate as a percentage out of 100.
     *
     * @return reported success rate of the project
     */
    public int getSuccessRate()
    {
        return Integer.parseInt(statusBox.getValue(PROPERTY_SUCCESS_RATE).split("%")[0].trim());
    }

    /**
     * Indicates if the project has any statistics displayed.
     *
     * @return true if there are displayed statistics for the project
     */
    public boolean hasStatistics()
    {
        return !StringUtils.equals(statusBox.getValue(PROPERTY_STATISTICS), "no builds");
    }

    /**
     * Indicates if there is any activity (queued or running builds) displayed
     * on the page.
     *
     * @return true if there is some build activity, false otherwise
     */
    public boolean hasBuildActivity()
    {
        return getActivityCount() > 0;
    }

    /**
     * Returns the number of entries in the current activity table (queued or
     * running builds).
     *
     * @return the number of entries in the current activity table
     */
    public int getActivityCount()
    {
        return activityTable.getRowCount();
    }

    /**
     * Returns the number of queued builds shown in the current activity table.
     *
     * @return the number of queued builds shown
     */
    public int getQueuedBuildCount()
    {
        return getActivityCountForState("queued");
    }

    /**
     * Returns the number of in progress builds shown in the current activity
     * table.
     *
     * @return the number of in progress builds shown
     */
    public int getActiveBuildCount()
    {
        return getActivityCountForState(ResultState.IN_PROGRESS.getPrettyString());
    }

    private int getActivityCountForState(String state)
    {
        int count = 0;
        int rows = activityTable.getRowCount();
        for (int i = 0; i < rows; i++)
        {
            Map<String,String> row = activityTable.getRow(i);
            if (row.get(COLUMN_ACTIVITY_STATUS).startsWith(state))
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Indicates if the latest completed build table is populated with a build.
     *
     * @return true if a latest completed build is present, false otherwise
     */
    public boolean hasLatestCompletedBuild()
    {
        return latestCompleteTable.isRowPresent(ID_LATEST_NUMBER);
    }

    /**
     * Gets the build number for the latest completed build.
     *
     * @return the build number for the latest completed build
     */
    public int getLatestCompletedBuildId()
    {
        String text = latestCompleteTable.getValue(ID_LATEST_NUMBER);
        String[] pieces = text.trim().split("\\s+");
        return Integer.parseInt(pieces[1]);
    }

    /**
     * Gets the status of the latest completed build.
     *
     * @return the result of the latest completed build
     */
    public ResultState getLatestCompletedBuildStatus()
    {
        return ResultState.fromPrettyString(latestCompleteTable.getValue(ID_LATEST_STATUS));
    }

    /**
     * Waits for a build of the given number to become the latest completed
     * build, and returns its result.
     *
     * @param id      number of the build to wait for
     * @param timeout maximum time to wait (in milliseconds)
     * @return the result of the build
     */
    public ResultState waitForLatestCompletedBuild(final int id, long timeout)
    {
        browser.refreshUntil(timeout, new Condition()
        {
            public boolean satisfied()
            {
                waitFor();
                return hasLatestCompletedBuild() && getLatestCompletedBuildId() == id;
            }
        }, "build " + id + " to complete");

        return getLatestCompletedBuildStatus();
    }

    /**
     * Indicates how many recently completed builds are shown (this does not
     * include the latest completed, which is shown in a separate table).
     *
     * @return the number of recently completed builds displayed
     */
    public int getRecentBuildsCount()
    {
        return recentTable.getRowCount();
    }

    /**
     * Returns information about the recent builds, from the most recent (first
     * row of the table) to least recent.  Note that the latest completed build
     * is not included.
     *
     * @return information about the latest completed builds
     */
    public List<BuildInfo> getRecentBuilds()
    {
        List<BuildInfo> result = new LinkedList<BuildInfo>();
        int count = getRecentBuildsCount();
        for (int i = 0; i < count; i++)
        {
            result.add(new BuildInfo(recentTable.getRow(i)));
        }

        return result;
    }

    /**
     * Indicates how many changes are shown for the project.
     *
     * @return the number of recent changes displayed
     */
    public int getChangesCount()
    {
        return changesTable.getRowCount();
    }

    /**
     * Returns information about recent changes, from the most recent (first
     * row of the table) to least recent.
     *
     * @return information about the latest changes
     */
    public List<Changelist> getChanges()
    {
        List<Changelist> result = new LinkedList<Changelist>();
        int count = getChangesCount();
        for (int i = 0; i < count; i++)
        {
            Map<String, String> row = changesTable.getRow(i);
            result.add(new Changelist(new Revision(row.get("revision")), 0, row.get("who"), row.get("comment"), Collections.<FileChange>emptyList()));
        }

        return result;
    }

    /**
     * Returns a list of all action labels displayed.  Note that these labels
     * are the text displayed to the user.
     *
     * @return the actions displayed on the right of the page
     */
    public List<String> getActions()
    {
        return actionsTable.getLinkLabels();
    }

    @Override
    protected String getActionId(String actionName)
    {
        return actionsTable.getLinkId(actionName);
    }

    public void triggerBuild()
    {
        actionsTable.clickLink(ACTION_TRIGGER);
    }

    public boolean isTriggerActionPresent()
    {
        return actionsTable.isLinkPresent(ACTION_TRIGGER);
    }

    public boolean isRebuildActionPresent()
    {
        return actionsTable.isLinkPresent(ACTION_REBUILD);
    }

    public boolean isViewWorkingCopyPresent()
    {
        return actionsTable.isLinkPresent(ACTION_VIEW_SOURCE);
    }

    public PulseFileSystemBrowserWindow viewWorkingCopy()
    {
        actionsTable.clickLink(ACTION_VIEW_SOURCE);
        return new PulseFileSystemBrowserWindow(browser);
    }

    /**
     * Returns a list of all links displayed.  Note that these labels are the
     * text displayed to the user.
     *
     * @return the actions displayed on the right of the page
     */
    public List<String> getLinks()
    {
        return linksTable.getLinkLabels();
    }

    public String getUrl()
    {
        return urls.project(uriComponentEncode(projectName));
    }
}
