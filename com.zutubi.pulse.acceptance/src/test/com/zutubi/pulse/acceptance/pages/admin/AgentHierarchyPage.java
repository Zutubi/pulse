package com.zutubi.pulse.acceptance.pages.admin;

import com.thoughtworks.selenium.Selenium;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The page shown when looking at an agent in the heirarchy view.
 */
public class AgentHierarchyPage extends HierarchyPage
{
    public AgentHierarchyPage(Selenium selenium, Urls urls, String agent, boolean template)
    {
        super(selenium, urls, MasterConfigurationRegistry.AGENTS_SCOPE, agent, template);
    }
}
