package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.server.ServerMessagesPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The agent messages page shows recent agent log messages.
 */
public class AgentMessagesPage extends ServerMessagesPage
{
    private String agent;

    public AgentMessagesPage(SeleniumBrowser browser, Urls urls, String agent, int page)
    {
        super(browser, urls, page);
        this.agent = agent;
    }

    public String getUrl()
    {
        if (getPage() == 1)
        {
            return urls.agentMessages(agent);
        }
        else
        {
            return urls.agentMessages(agent) + Integer.toString(getPage() - 1) + "/";
        }
    }

    @Override
    public ServerMessagesPage createNextPage()
    {
        return new AgentMessagesPage(browser, urls, agent, getPage() + 1);
    }
}
