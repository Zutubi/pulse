package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * The page shown when looking at an agent in the configuration view.
 */
public class AgentConfigPage extends CompositePage
{
    private String agent;
    private boolean template;

    public AgentConfigPage(Selenium selenium, Urls urls, String agent, boolean template)
    {
        super(selenium, urls, getPath(agent));
        this.agent = agent;
        this.template = template;
    }

    private static String getPath(String project)
    {
        return "agents/" + project;
    }

    public String getPath()
    {
        return getPath(agent);
    }

    public String getUrl()
    {
        return urls.adminAgent(agent);
    }

    public AgentHierarchyPage clickHierarchy()
    {
        selenium.click(super.getHierarchyLocator());
        return new AgentHierarchyPage(selenium, urls, agent, template);
    }
}
