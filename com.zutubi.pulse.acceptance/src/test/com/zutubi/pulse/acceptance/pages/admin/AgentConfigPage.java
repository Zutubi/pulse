package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * The page shown when looking at an agent in the configuration view.
 */
public class AgentConfigPage extends CompositePage
{
    private String agent;
    private boolean template;

    public AgentConfigPage(SeleniumBrowser browser, Urls urls, String agent, boolean template)
    {
        super(browser, urls, getPath(agent));
        this.agent = agent;
        this.template = template;
    }

    private static String getPath(String agent)
    {
        return "agents/" + agent;
    }

    public String getPath()
    {
        return getPath(agent);
    }

    public String getUrl()
    {
        return urls.adminAgent(WebUtils.uriComponentEncode(agent));
    }

    public AgentHierarchyPage clickHierarchy()
    {
        browser.click(super.getHierarchyLocator());
        return browser.createPage(AgentHierarchyPage.class, agent, template);
    }
}
