package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.pulse.project.CommentList;
import com.zutubi.pulse.acceptance.components.pulse.project.FeatureList;
import com.zutubi.pulse.acceptance.components.pulse.project.StatusBox;
import com.zutubi.pulse.acceptance.components.pulse.project.TestFailuresTable;
import com.zutubi.pulse.acceptance.components.table.LinkTable;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;
import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.webwork.Urls;
import static com.zutubi.util.WebUtils.uriComponentEncode;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends ResponsibilityPage
{
    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_TESTS = "tests";

    private String projectName;
    private long buildId;

    private StatusBox statusBox;
    private PropertyTable detailsTable;
    private CommentList commentList;
    private FeatureList errors;
    private FeatureList warnings;
    private TestFailuresTable testFailuresTable;
    private LinkTable actionsTable;
    private LinkTable linksTable;
    private LinkTable artifactsTable;
    private LinkTable hooksTable;

    public BuildSummaryPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, projectName + "-build-" + Long.toString(buildId) + "-summary", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
        
        statusBox = new StatusBox(browser, "build-summary-status");
        detailsTable = new PropertyTable(browser, "build-summary-details");
        commentList = new CommentList(browser, "build-summary-comments");
        errors = new FeatureList(browser, "build-summary-errors");
        warnings = new FeatureList(browser, "build-summary-warnings");
        testFailuresTable = new TestFailuresTable(browser, "build-summary-failures");
        actionsTable = new LinkTable(browser, "build-summary-actions");
        linksTable = new LinkTable(browser, "build-summary-links");
        artifactsTable = new LinkTable(browser, "build-summary-artifacts");
        hooksTable = new LinkTable(browser, "build-summary-hooks");
    }

    public String getUrl()
    {
        return urls.buildSummary(uriComponentEncode(projectName), Long.toString(buildId));
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.initialised");
    }

    /**
     * Returns the number for the build.
     *
     * @return the build number
     */
    public long getBuildId()
    {
        return buildId;
    }

    /**
     * Indicates if the right-hand panel (with actions, links etc) is visible.
     *
     * @return true if the panel is visible, false if it is collapsed
     */
    public boolean isRightPaneVisible()
    {
        return Boolean.valueOf(browser.evalExpression(SeleniumBrowser.CURRENT_WINDOW + ".Ext.getCmp('build-summary-right').isVisible()"));
    }
    
    public ResultState getBuildStatus()
    {
        return ResultState.fromPrettyString(statusBox.getValue(PROPERTY_STATUS));
    }

    public String getTestsSummary()
    {
        return statusBox.getValue(PROPERTY_TESTS);
    }

    public void waitForComments(long timeout)
    {
        commentList.waitFor(timeout);
    }
    
    public boolean isCommentsPresent()
    {
        return commentList.isPresent();
    }

    /**
     * Indicates if a comment with the given id is present.
     *
     * @param commentId unique id of the comment
     * @return true if the comment is present, false otherwise
     */
    public boolean isCommentPresent(long commentId)
    {
        return commentList.isCommentPresent(commentId);
    }

    /**
     * Indicates if a delete link is shown for the comment of the given id.
     *
     * @param commentId unique id of the comment
     * @return true if the given comment has a delete link
     */
    public boolean isCommentDeleteLinkPresent(long commentId)
    {
        return commentList.isDeleteLinkPresent(commentId);
    }

    /**
     * Clicks the delete link for the comment of the given id.
     *
     * @param commentId unique id of the comment
     * @return a confirmation dialog that will be poppe up on clicking the link
     */
    public ConfirmDialog clickDeleteComment(long commentId)
    {
        commentList.clickDeleteLink(commentId);
        return new ConfirmDialog(browser);
    }

    public boolean isErrorListPresent()
    {
        return errors.isPresent();
    }

    public boolean isWarningListPresent()
    {
        return warnings.isPresent();
    }

    public boolean isTestFailuresTablePresent()
    {
        return testFailuresTable.isPresent();
    }

    @Override
    protected String getActionId(String actionName)
    {
        return actionsTable.getLinkId(actionName);
    }

    @Override
    public boolean isActionPresent(String actionName)
    {
        return actionsTable.isLinkPresent(actionName);
    }

    @Override
    public void clickAction(String actionName)
    {
        actionsTable.clickLink(actionName);
    }

    public boolean isRelatedLinksTablePresent()
    {
        return linksTable.isPresent();
    }
    
    public String getRelatedLinkText(int index)
    {
        return linksTable.getLinkLabel(index);
    }

    public boolean isFeaturedArtifactsTablePresent()
    {
        return artifactsTable.isPresent();
    }

    public String getFeaturedArtifactsRow(int index)
    {
        return artifactsTable.getLinkLabel(index);
    }

    public boolean isHookPresent(String hookName)
    {
        return hooksTable.isLinkPresent(hookName);
    }

    public void clickHook(String hookName)
    {
        hooksTable.clickLink(hookName);
    }
}
