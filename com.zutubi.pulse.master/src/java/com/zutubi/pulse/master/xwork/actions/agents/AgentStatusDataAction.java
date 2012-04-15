package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.AgentStatus;
import com.zutubi.pulse.master.agent.DefaultAgent;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.CommentModel;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.time.TimeStamps;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to view the status of a single agent.
 */
public class AgentStatusDataAction extends AgentActionBase
{
    private static final Messages I18N = Messages.getInstance(AgentStatusDataAction.class);

    private AgentStatusModel model;
    private BuildManager buildManager;
    private MasterConfigurationManager configurationManager;
    private SystemPaths systemPaths;
    private ActionManager actionManager;

    public AgentStatusModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        DefaultAgent agent = (DefaultAgent) getRequiredAgent();
        Host host = agent.getHost();
        model = new AgentStatusModel(agent.getName(), host.getLocation());

        if (agent.isEnabled())
        {
            String prettyStatus;
            if (agent.isDisabling())
            {
                prettyStatus = I18N.format("disable.on.idle");
            }
            else
            {
                prettyStatus = agent.getStatus().getPrettyString();
            }

            addAgentStatus(agent.getStatus(), prettyStatus);

            if (host.isUpgrading())
            {
                if (host.getPersistentUpgradeState() == HostState.PersistentUpgradeState.FAILED_UPGRADE)
                {
                    model.addStatus(I18N.format("host.status"), I18N.format("host.failed.upgrade.verbose"));
                    if (host.getUpgradeMessage() != null)
                    {
                        model.addStatus(I18N.format("host.upgrade.message"), host.getUpgradeMessage());
                    }
                }
                else
                {
                    model.addStatus(I18N.format("host.status"), I18N.format("host.upgrade.inprogress"));
                    model.addStatus(I18N.format("host.upgrade.status"), EnumUtils.toPrettyString(host.getUpgradeState()));

                    int progress = host.getUpgradeProgress();
                    if (progress > 0)
                    {
                        model.addStatus(I18N.format("host.upgrade.progress"), Integer.toString(progress) + "%");
                    }
                }
            }
            else if (agent.hasBeenPinged())
            {
                model.addStatus(I18N.format("agent.last.ping"), TimeStamps.getPrettyDate(agent.getLastPingTime(), getLocale()));
                model.addStatus(I18N.format("agent.since.last.ping"), Long.toString(agent.getSecondsSincePing()) + " seconds");
                if (agent.getPingError() != null)
                {
                    model.addStatus(I18N.format("agent.ping.error"), agent.getPingError());
                }

                long recipeId = agent.getRecipeId();
                if (recipeId != HostStatus.NO_RECIPE)
                {
                    BuildResult buildResult = buildManager.getByRecipeId(recipeId);
                    if (buildResult != null)
                    {
                        RecipeResultNode node = buildResult.findResultNodeByRecipeId(recipeId);
                        if (node != null)
                        {
                            Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
                            model.setExecutingStage(new ExecutingStageModel(buildResult, node, urls));
                        }
                    }
                }
            }
        }
        else
        {
            addAgentStatus(agent.getStatus(), agent.getStatus().getPrettyString());
        }

        final Messages messages = Messages.getInstance(AgentConfiguration.class);
        File contentRoot = systemPaths.getContentRoot();
        for (String actionName: actionManager.getActions(agent.getConfig(), false, true))
        {
            model.addAction(ToveUtils.getActionLink(actionName, messages, contentRoot));
        }

        if (getLoggedInUser() != null)
        {
            model.addAction(ToveUtils.getActionLink(CommentContainer.ACTION_ADD_COMMENT, messages, contentRoot));
        }

        Actor actor = accessManager.getActor();
        for (Comment comment: agent.getComments())
        {
            model.addComment(new CommentModel(comment, accessManager.hasPermission(actor, AccessManager.ACTION_DELETE, comment)));
        }

        List<AgentSynchronisationMessage> synchronisationMessages = new LinkedList<AgentSynchronisationMessage>(agentManager.getSynchronisationMessages(agent.getId()));
        Collections.reverse(synchronisationMessages);
        model.addSynchronisationMessages(synchronisationMessages);

        return SUCCESS;
    }

    private void addAgentStatus(AgentStatus status, String prettyStatus)
    {
        model.addStatus(I18N.format("agent.status"), prettyStatus);
        String descriptionKey = status.name() + ".description";
        if (I18N.isKeyDefined(descriptionKey))
        {
            model.addStatus(I18N.format("agent.status.description"), I18N.format(descriptionKey));
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
