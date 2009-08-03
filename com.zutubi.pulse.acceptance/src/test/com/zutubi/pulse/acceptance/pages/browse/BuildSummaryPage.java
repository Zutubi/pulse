package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * The summary tab for a build result.
 */
public class BuildSummaryPage extends ResponsibilityPage
{
    private static final String DEPENDENTY_TABLE_ID = "table.dependencies";

    private String projectName;
    private long buildId;

    public BuildSummaryPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-summary", "build " + buildId);
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
        return StringUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-dependencies";
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

    public class DependencyRow
    {
        private String stage;
        private String project;
        private String build;
        private String artifact;

        public DependencyRow(String stage, String project, String build, String artifact)
        {
            this.stage = stage;
            this.project = project;
            this.build = build;
            this.artifact = artifact;
        }

        public String getStage()
        {
            return stage;
        }

        public String getProject()
        {
            return project;
        }

        public String getBuild()
        {
            return build;
        }

        public String getArtifact()
        {
            return artifact;
        }
    }
}
