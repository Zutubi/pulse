package com.zutubi.pulse.acceptance.pages.browse;

import com.google.common.base.Objects;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.TextBox;
import com.zutubi.pulse.acceptance.components.pulse.project.BuildSummaryTable;
import com.zutubi.pulse.acceptance.components.pulse.project.StatusBox;
import com.zutubi.pulse.acceptance.components.table.LinkTable;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.pulse.acceptance.windows.PulseFileSystemBrowserWindow;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Condition;
import com.zutubi.util.StringUtils;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.ACTION_VIEW_SOURCE;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * The project home page is a summary of the state and recent activity for a
 * project.
 */
public class ProjectHomePage extends ResponsibilityPage
{
    private static final Messages PROJECT_I18N = Messages.getInstance(ProjectConfiguration.class);

    private static final String PROPERTY_HEALTH = "health";
    private static final String PROPERTY_STATE = "state";
    private static final String PROPERTY_SUCCESS_RATE = "successRate";
    private static final String PROPERTY_STATISTICS = "statistics";

    private static final String COLUMN_ACTIVITY_STATUS = "status";

    private static final Pattern BUILD_NUMBER_PATTERN = Pattern.compile("build (\\d+)");
    
    private String projectName;
    private StatusBox statusBox;
    private SummaryTable activityTable;
    private BuildSummaryTable recentTable;
    private SummaryTable changesTable;
    private TextBox descriptionBox;
    private LinkTable actionsTable;
    private LinkTable linksTable;
    private LinkTable artifactsTable;

    public ProjectHomePage(SeleniumBrowser browser, Urls urls, String projectName)
    {
        super(browser, urls, "project-home-" + projectName, projectName);
        this.projectName = projectName;
        statusBox = new StatusBox(browser, "project-home-status");
        activityTable = new SummaryTable(browser, "project-home-activity");
        recentTable = new BuildSummaryTable(browser, "project-home-recent");
        changesTable = new SummaryTable(browser, "project-home-changes");
        descriptionBox = new TextBox(browser, "project-home-description");
        actionsTable = new LinkTable(browser, "project-home-actions");
        linksTable = new LinkTable(browser, "project-home-links");
        artifactsTable = new LinkTable(browser, "project-home-artifacts");
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel");
        browser.waitForVariable("panel.initialised");
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
        state = StringUtils.stripSuffix(state, "reinitialise");
        state = state.trim();
        return state;
    }

    /**
     * Indicates if a link is present for the given project transition.
     *
     * @param transition the transition to check for
     * @return true if there is a link to make the given transition
     */
    public boolean isTransitionLinkPresent(Project.Transition transition)
    {
        return browser.isElementIdPresent(getTransitionId(transition));
    }

    /**
     * Clicks the link for the given project transition.
     *
     * @param transition the transition to trigger
     */
    public void clickTransitionLink(Project.Transition transition)
    {
        browser.click(By.id(getTransitionId(transition)));
    }

    private String getTransitionId(Project.Transition transition)
    {
        return "project-transition-" + transition.toString().toLowerCase();
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
        return !Objects.equal(statusBox.getValue(PROPERTY_STATISTICS), "no builds");
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
    public long getActivityCount()
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
        long rows = activityTable.getRowCount();
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
     * Indicates if the recent builds table is populated with at least one build.
     *
     * @return true if the recent builds table is non-empty
     */
    public boolean hasCompletedBuild()
    {
        return recentTable.getRowCount() > 0;
    }

    /**
     * Gets the build number for the latest completed build.
     *
     * @return the build number for the latest completed build
     */
    public int getLatestCompletedBuildId()
    {
        return recentTable.getBuilds().get(0).number;
    }

    /**
     * Gets the status of the latest completed build.
     *
     * @return the result of the latest completed build
     */
    public ResultState getLatestCompletedBuildStatus()
    {
        return recentTable.getBuilds().get(0).status;
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
                return hasCompletedBuild() && getLatestCompletedBuildId() == id;
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
    public long getRecentBuildsCount()
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
        return recentTable.getBuilds();
    }

    /**
     * Indicates how many changes are shown for the project.
     *
     * @return the number of recent changes displayed
     */
    public long getChangesCount()
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
        long count = getChangesCount();
        for (int i = 0; i < count; i++)
        {
            Map<String, String> row = changesTable.getRow(i);
            result.add(new Changelist(new Revision(row.get("revision")), 0, row.get("who"), row.get("comment"), Collections.<FileChange>emptyList()));
        }

        return result;
    }

    /**
     * Indicates if a description is displayed.
     * 
     * @return true if a description is present for this project
     */
    public boolean hasDescription()
    {
        return descriptionBox.isPresent();
    }

    /**
     * Retrieves the project's description.
     * 
     * @return the project description
     */
    public String getDescription()
    {
        return descriptionBox.getText();
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
        return actionsTable.getLinkId(formatAction(actionName));
    }

    private String formatAction(String actionName)
    {
        String key = actionName + ".label";
        if (PROJECT_I18N.isKeyDefined(key))
        {
            return PROJECT_I18N.format(key);
        }
        else
        {
            return actionName;
        }
    }

    public void triggerBuild()
    {
        actionsTable.clickLink(ProjectManager.DEFAULT_TRIGGER_NAME);
    }

    public boolean isTriggerActionPresent()
    {
        return isActionPresent(ProjectManager.DEFAULT_TRIGGER_NAME);
    }

    public boolean isViewWorkingCopyPresent()
    {
        return isActionPresent(ACTION_VIEW_SOURCE);
    }

    public PulseFileSystemBrowserWindow viewWorkingCopy()
    {
        clickAction(ACTION_VIEW_SOURCE);
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

    /**
     * Indicates if the latest featured artifacts table is present.
     * 
     * @return true if the latest featured artifacts table is present
     */
    public boolean hasFeaturedArtifacts()
    {
        return artifactsTable.isPresent();
    }

    /**
     * Returns the number of the build that featured artifacts are displayed
     * for.
     * 
     * @return build number for the build that the featured artifacts are from
     */
    public int getFeaturedArtifactsBuild()
    {
        String title = artifactsTable.getTitle();
        Matcher matcher = BUILD_NUMBER_PATTERN.matcher(title);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }

    /**
     * Returns the text in the content rows of the featured artifacts table.
     * This includes both the categories (stages) and the links (artifacts).
     * 
     * @return text content of the rows of the featured artifacts table
     */
    public List<String> getFeaturedArtifactsRows()
    {
        return artifactsTable.getLinkLabels();
    }

    public String getUrl()
    {
        return urls.project(uriComponentEncode(projectName));
    }
}
