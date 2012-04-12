package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The statistics tab for a specific agent.
 */
public class AgentStatisticsPage extends SeleniumPage
{
    public static final String ID_PAGE = "agent-statistics";
    public static final String ID_RECIPE_STATS = ID_PAGE + "-recipes";
    public static final String ID_USAGE_STATS = ID_PAGE + "-utilisation";
    public static final String ID_USAGE_CHART = ID_PAGE + "-usageGraph";

    private String agent;

    public AgentStatisticsPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls, ID_PAGE, "agent statistics");
        this.agent = agent;
    }

    public String getUrl()
    {
        return urls.agentStatistics(agent);
    }

    public boolean isRecipeStatisticsPresent()
    {
        return browser.isElementIdPresent(ID_RECIPE_STATS);
    }

    public boolean isUsageStatisticsPresent()
    {
        return browser.isElementIdPresent(ID_USAGE_STATS);
    }

    public boolean isUsageChartPresent()
    {
        return browser.isElementIdPresent(ID_USAGE_CHART);
    }
}