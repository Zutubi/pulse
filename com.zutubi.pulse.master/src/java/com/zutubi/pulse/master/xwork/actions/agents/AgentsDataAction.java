package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationFormatter;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;

import java.io.File;
import java.util.*;

/**
 * An action to display all agents attached to this master, including local
 * agents.
 */
public class AgentsDataAction extends ActionSupport
{
    private AgentsModel model;
    
    private AgentManager agentManager;
    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private ConfigurationTemplateManager configurationTemplateManager;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;

    public AgentsModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        model = new AgentsModel();

        List<Agent> agents = agentManager.getAllAgents();
        // Find invalid agents (which the agent manager always ignores).
        Collection<AgentConfiguration> allConfigs = configurationTemplateManager.getAllInstances(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), AgentConfiguration.class, true);
        List<String> invalidAgents = new LinkedList<String>();
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
                invalidAgents.add(config.getName());
            }
        }

        final Sort.StringComparator stringComparator = new Sort.StringComparator();
        Collections.sort(invalidAgents, stringComparator);
        model.addInvalidAgents(invalidAgents);
        
        final Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        final Messages messages = Messages.getInstance(AgentConfiguration.class);
        final AgentConfigurationFormatter formatter = new AgentConfigurationFormatter();
        List<AgentRowModel> rows = new LinkedList<AgentRowModel>();
        for (Agent agent: agents)
        {
            AgentRowModel rowModel = new AgentRowModel(agent.getId(), agent.getConfig().getName(), agent.getHost().getLocation(), formatter.getStatus(agent));
            addExecutingBuild(agent, rowModel, urls);
            addActions(agent, rowModel, messages);
            rows.add(rowModel);
        }

        Collections.sort(rows, new Comparator<AgentRowModel>()
        {
            public int compare(AgentRowModel o1, AgentRowModel o2)
            {
                return stringComparator.compare(o1.getName(), o2.getName());
            }
        });
        
        model.addAgents(rows);
        return SUCCESS;
    }

    private void addExecutingBuild(Agent agent, AgentRowModel rowModel, Urls urls)
    {
        long recipeId = agent.getRecipeId();
        if (recipeId != HostStatus.NO_RECIPE)
        {
            BuildResult buildResult = buildManager.getByRecipeId(recipeId);
            if (buildResult != null)
            {
                RecipeResultNode node = buildResult.findResultNodeByRecipeId(recipeId);
                if (node != null)
                {
                    rowModel.setExecutingStage(new ExecutingStageModel(buildResult, node, urls));
                }
            }
        }
    }

    private void addActions(Agent agent, AgentRowModel rowModel, Messages messages)
    {
        File contentRoot = systemPaths.getContentRoot();
        for (String actionName: actionManager.getActions(agent.getConfig(), false, true))
        {
            rowModel.addAction(ToveUtils.getActionLink(actionName, messages, contentRoot));
        }
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
