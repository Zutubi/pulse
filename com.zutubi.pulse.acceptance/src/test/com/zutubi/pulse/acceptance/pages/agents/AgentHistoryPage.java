package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.AbstractHistoryPage;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * Shows completed builds for that involved an agent.
 */
public class AgentHistoryPage extends AbstractHistoryPage
{
    private String agentName;

    public AgentHistoryPage(SeleniumBrowser browser, Urls urls, String agentName)
    {
        super(browser, urls, "agent-history-" + agentName, "agent-history");
        this.agentName = agentName;
    }

    @Override
    public String getUrl()
    {
        return urls.agentHistory(WebUtils.uriComponentEncode(agentName));
    }
}
