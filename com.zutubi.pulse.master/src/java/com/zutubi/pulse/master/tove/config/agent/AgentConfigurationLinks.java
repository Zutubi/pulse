package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.links.ConfigurationLink;
import com.zutubi.util.WebUtils;

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
            Urls urls = Urls.getBaselessInstance();
            String name = WebUtils.uriComponentEncode(agentConfiguration.getName());
            return Arrays.asList(
                    new ConfigurationLink("status", urls.agentStatus(name)),
                    new ConfigurationLink("statistics", urls.agentStatistics(name)),
                    new ConfigurationLink("history", urls.agentHistory(name)),
                    new ConfigurationLink("messages", urls.agentMessages(name)),
                    new ConfigurationLink("info", urls.agentInfo(name))
            );
        }
        else
        {
            return Collections.emptyList();
        }
    }
}