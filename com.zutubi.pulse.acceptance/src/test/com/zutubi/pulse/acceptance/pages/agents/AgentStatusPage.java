package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The status tab for a specific agent.
 */
public class AgentStatusPage extends SeleniumPage
{
    public static final String ID_STATUS_TABLE = "agent.status";
    public static final String ID_BUILD_TABLE = "executing.build.stage";

    private String agent;

    public AgentStatusPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls, ID_STATUS_TABLE, "agent status");
        this.agent = agent;
    }

    public String getUrl()
    {
        return urls.agentStatus(agent);
    }

    public boolean isExecutingBuildPresent()
    {
        return browser.isElementPresent(ID_BUILD_TABLE);
    }

    public String getExecutingProject()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 1, 1);
    }

    public String getExecutingOwner()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 2, 1);
    }

    public String getExecutingId()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 3, 1);
    }

    public String getExecutingStage()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 5, 1);
    }

    public String getExecutingRecipe()
    {
        return browser.getCellContents(ID_BUILD_TABLE, 6, 1);
    }
}
