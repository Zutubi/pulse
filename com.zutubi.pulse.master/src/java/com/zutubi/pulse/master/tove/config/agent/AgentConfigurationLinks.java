package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.links.ConfigurationLink;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Links from the agent configuration page to related agent pages.
 */
public class AgentConfigurationLinks
{
    public List<ConfigurationLink> getLinks(AgentConfiguration agentConfiguration)
    {
        if (agentConfiguration.isConcrete())
        {
            Urls urls = new Urls("");
            String name = agentConfiguration.getName();
            return Arrays.asList(
                    new ConfigurationLink("status", urls.agentStatus(name), "cog.gif"),
                    new ConfigurationLink("messages", urls.agentMessages(name), "script.gif"),
                    new ConfigurationLink("info", urls.agentInfo(name), "information.gif")
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }
}