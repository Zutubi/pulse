package com.zutubi.pulse.acceptance.pages.browse;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Pair;
import com.zutubi.util.WebUtils;

/**
 * The details tab for a build result.
 */
public class BuildDetailsPage extends AbstractBuildStatusPage
{
    public static final String ID_CUSTOM_FIELDS = "custom-fields";
    public static final String ID_RETRIEVED_DEPENDENCIES = "build-dependencies";
    public static final String ID_STAGE_BASICS = "stage-basics";
    public static final String ID_COMMAND_BASICS = "command-basics";
    public static final String ID_COMMAND_PROPERTIES = "command-properties";

    public BuildDetailsPage(SeleniumBrowser browser, Urls urls, String projectName, long buildId)
    {
        super(browser, urls, WebUtils.uriComponentEncode(projectName) + "-build-" + Long.toString(buildId) + "-details", WebUtils.uriComponentEncode(projectName), projectName, buildId);
    }

    public String getUrl()
    {
        return urls.buildDetails(projectName, Long.toString(buildId));
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        waitForPaneToLoad();
    }

    public void clickStageAndWait(String stageName)
    {
        browser.click("xpath=//a/span[text()='" + stageName + "']");
        waitForPaneToLoad();
    }

    public void clickCommandAndWait(String stageName, String commandName)
    {
        browser.click("xpath=//a/span[text()='" + stageName + "']/ancestor::li[1]//a/span[text()='" + commandName + "']");
        waitForPaneToLoad();
    }

    private void waitForPaneToLoad()
    {
        browser.waitForCondition("selenium.browserbot.getCurrentWindow().paneLoading === false");
    }

    public boolean isCustomFieldsTablePresent()
    {
        return browser.isElementPresent(ID_CUSTOM_FIELDS);
    }

    public Pair<String, String> getCustomField(int index)
    {
        return getRow(ID_CUSTOM_FIELDS, index);
    }
    
    public String getCustomFieldValue(int index)
    {
        return browser.getCellContents(ID_CUSTOM_FIELDS, index + 1, 1);
    }

    public boolean isDependenciesTablePresent()
    {
        return browser.isElementPresent(ID_RETRIEVED_DEPENDENCIES);
    }

    public DependencyRow getDependencyRow(int row)
    {
        row = row + 1; // skip the table header row.
        return new DependencyRow(
                browser.getCellContents(ID_RETRIEVED_DEPENDENCIES, row, 0),
                browser.getCellContents(ID_RETRIEVED_DEPENDENCIES, row, 1),
                browser.getCellContents(ID_RETRIEVED_DEPENDENCIES, row, 2),
                browser.getCellContents(ID_RETRIEVED_DEPENDENCIES, row, 3)
        );
    }

    public boolean isStageBasicsPresent()
    {
        return browser.isElementPresent(ID_STAGE_BASICS);
    }
    
    public Pair<String, String> getStageBasicsRow(int index)
    {
        return getRow(ID_STAGE_BASICS, index);
    }
    
    public boolean isCommandBasicsPresent()
    {
        return browser.isElementPresent(ID_COMMAND_BASICS);
    }
    
    public Pair<String, String> getCommandBasicsRow(int index)
    {
        return getRow(ID_COMMAND_BASICS, index);
    }

    public boolean isCommandPropertiesPresent()
    {
        return browser.isElementPresent(ID_COMMAND_PROPERTIES);
    }
    
    public Pair<String, String> getCommandPropertiesRow(int index)
    {
        return getRow(ID_COMMAND_PROPERTIES, index);
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
