package com.zutubi.pulse.acceptance.pages.admin;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.master.webwork.Urls;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;

/**
 * The page shown when looking at an agent in the hierarchy view.
 */
public class AgentHierarchyPage extends HierarchyPage
{
    public AgentHierarchyPage(SeleniumBrowser browser, Urls urls, String agent, boolean template)
    {
        super(browser, urls, AGENTS_SCOPE, agent, template);
    }
}
