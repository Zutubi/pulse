package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.ConfirmDialog;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends ResponsibilityPage
{
    private static final String DEPENDENTY_TABLE_ID = "table.dependencies";
    private static final String COMMENTS_LIST_ID = "build.comments";

    private String projectName;
    private long buildId;

    public BuildSummaryPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, WebUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-summary", "build " + buildId);
        this.projectName = projectName;
        this.buildId = buildId;
    }

    public String getUrl()
    {
        return urls.buildSummary(projectName, Long.toString(buildId));
    }

    private String getHookId(String hookName)
    {
        return "hook." + hookName;
    }

    public boolean isHookPresent(String hookName)
    {
        return browser.isElementIdPresent(getHookId(hookName));
    }

    public void clickHook(String hookName)
    {
        browser.click(getHookId(hookName));
    }

    public boolean hasTests()
    {
        return !getSummaryTestsColumnText().contains("none");
    }

    public String getSummaryTestsColumnText()
    {
        return browser.getText("id="+projectName+".build."+buildId+".test");
    }

    private String getDependenciesId()
    {
        return WebUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-dependencies";
    }

    public boolean hasDependencies()
    {
        return browser.isElementIdPresent(getDependenciesId());
    }

    public DependencyRow getDependencyRow(int row)
    {
        row = row + 1; // skip the table header row.
        return new DependencyRow(
                browser.getCellContents(DEPENDENTY_TABLE_ID, row, 0),
                browser.getCellContents(DEPENDENTY_TABLE_ID, row, 1),
                browser.getCellContents(DEPENDENTY_TABLE_ID, row, 2),
                browser.getCellContents(DEPENDENTY_TABLE_ID, row, 3)
        );
    }

    public int getDependencyCount()
    {
        return 0;
    }

    public boolean isCommentsPresent()
    {
        return browser.isElementPresent(COMMENTS_LIST_ID);
    }

    public boolean isDeleteCommentLinkPresent(int commentNumber)
    {
        return browser.isElementPresent(getDeleteCommentLinkId(commentNumber));
    }

    public ConfirmDialog clickDeleteComment(int commentNumber)
    {
        browser.click(getDeleteCommentLinkId(commentNumber));
        return new ConfirmDialog(browser);
    }

    private String getDeleteCommentLinkId(int commentNumber)
    {
        return "delete.comment." + commentNumber;
    }

    public class DependencyRow
    {
        private String project;
        private String build;
        private String stage;
        private String artifact;

        public DependencyRow(String project, String build, String stage, String artifact)
        {
            this.project = project;
            this.build = build;
            this.stage = stage;
            this.artifact = artifact;
        }

        public String getProject()
        {
            return project;
        }

        public String getBuild()
        {
            return build;
        }

        public String getStage()
        {
            return stage;
        }

        public String getArtifact()
        {
            return artifact;
        }
    }
}
