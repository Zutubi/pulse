package com.zutubi.pulse.web.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.webwork.ToveUtils;
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
    private List<AgentModel> models;
    private List<AgentConfiguration> invalidAgents = new LinkedList<AgentConfiguration>();
    private AgentManager agentManager;
    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private ConfigurationTemplateManager configurationTemplateManager;

    public List<AgentModel> getModels()
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
        Collection<AgentConfiguration> allConfigs = configurationTemplateManager.getAllInstances(PathUtils.getPath(ConfigurationRegistry.AGENTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), AgentConfiguration.class, true);
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

        models = CollectionUtils.map(agents, new Mapping<Agent, AgentModel>()
        {
            public AgentModel map(Agent agent)
            {
                String status;
                if(agent.isDisabling())
                {
                    status = "disabling on idle";
                }
                else if(agent.isUpgrading())
                {
                    status = "upgrading [" + agent.getUpgradeState().toString().toLowerCase() + "]";
                }
                else
                {
                    status = agent.getStatus().getPrettyString();
                }

                AgentModel model = new AgentModel(agent, agent.getConfig().getName(), agent.getLocation(), status);

                Messages messages = Messages.getInstance(AgentConfiguration.class);
                File contentRoot = systemPaths.getContentRoot();
                for(String actionName: actionManager.getActions(agent.getConfig(), false))
                {
                    model.addAction(ToveUtils.getActionLink(actionName, messages, contentRoot));
                }

                return model;
            }
        });

        Collections.sort(models, new Comparator<AgentModel>()
        {
            public int compare(AgentModel o1, AgentModel o2)
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
