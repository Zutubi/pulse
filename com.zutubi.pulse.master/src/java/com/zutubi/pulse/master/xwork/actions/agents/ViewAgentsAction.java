package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationFormatter;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;

import java.io.File;
import java.util.*;

/**
 * An action to display all agents attached to this master, including local
 * agents.
 */
public class ViewAgentsAction extends ActionSupport
{
    private List<AgentRowModel> models;
    private List<AgentConfiguration> invalidAgents = new LinkedList<AgentConfiguration>();
    private AgentManager agentManager;
    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private ConfigurationTemplateManager configurationTemplateManager;

    public List<AgentRowModel> getModels()
    {
        return models;
    }

    public List<AgentConfiguration> getInvalidAgents()
    {
        return invalidAgents;
    }

    public String execute() throws Exception
    {
        List<Agent> agents = agentManager.getAllAgents();

        // Find invalid agents (which the agent manager always ignores).
        Collection<AgentConfiguration> allConfigs = configurationTemplateManager.getAllInstances(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), AgentConfiguration.class, true);
        for (final AgentConfiguration config: allConfigs)
        {
            if (config.isConcrete() && !CollectionUtils.contains(agents, new Predicate<Agent>()
            {
                public boolean satisfied(Agent agent)
                {
                    return agent.getConfig().equals(config);
                }
            }))
            {
                invalidAgents.add(config);
            }
        }

        final Comparator<String> c = new Sort.StringComparator();
        Collections.sort(invalidAgents, new Comparator<AgentConfiguration>()
        {
            public int compare(AgentConfiguration o1, AgentConfiguration o2)
            {
                return c.compare(o1.getName(), o2.getName());
            }
        });

        final AgentConfigurationFormatter formatter = new AgentConfigurationFormatter();
        models = CollectionUtils.map(agents, new Mapping<Agent, AgentRowModel>()
        {
            public AgentRowModel map(Agent agent)
            {
                AgentRowModel rowModel = new AgentRowModel(agent, agent.getConfig().getName(), agent.getHost().getLocation(), formatter.getStatus(agent));

                Messages messages = Messages.getInstance(AgentConfiguration.class);
                File contentRoot = systemPaths.getContentRoot();
                for(String actionName: actionManager.getActions(agent.getConfig(), false))
                {
                    rowModel.addAction(ToveUtils.getActionLink(actionName, messages, contentRoot));
                }

                return rowModel;
            }
        });

        Collections.sort(models, new Comparator<AgentRowModel>()
        {
            public int compare(AgentRowModel o1, AgentRowModel o2)
            {
                return c.compare(o1.getName(), o2.getName());
            }
        });
        return SUCCESS;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
