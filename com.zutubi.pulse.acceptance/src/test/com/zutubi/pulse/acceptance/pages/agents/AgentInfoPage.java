package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.pages.server.ServerInfoPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The agent info page shows system information for the agent server and VM.
 */
public class AgentInfoPage extends ServerInfoPage
{
    private String agent;

    public AgentInfoPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls);
        this.agent = agent;
    }

    public String getUrl()
    {
        return urls.agentInfo(agent);
    }
}
