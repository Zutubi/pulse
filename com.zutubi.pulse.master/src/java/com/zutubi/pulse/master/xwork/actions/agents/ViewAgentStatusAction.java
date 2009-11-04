package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.agent.DefaultAgent;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.TimeStamps;

/**
 * Action to view the status of a single agent.
 */
public class ViewAgentStatusAction extends AgentActionBase
{
    private static final Messages I18N = Messages.getInstance(ViewAgentStatusAction.class);

    private AgentStatusModel model;
    private BuildManager buildManager;

    public AgentStatusModel getModel()
    {
        return model;
    }

    public String execute() throws Exception
    {
        DefaultAgent agent = (DefaultAgent) getRequiredAgent();
        Host host = agent.getHost();
        model = new AgentStatusModel(agent, host.getLocation());
        if (agent.isEnabled())
        {
            String status;
            if (agent.isDisabling())
            {
                status = I18N.format("disable.on.idle");
            }
            else
            {
                status = agent.getStatus().getPrettyString();
            }

            model.addStatusInfo("agent.status", status);

            if (host.isUpgrading())
            {
                if (host.getPersistentUpgradeState() == HostState.PersistentUpgradeState.FAILED_UPGRADE)
                {
                    model.addStatusInfo("host.status", I18N.format("host.failed.upgrade.verbose"));
                    if (host.getUpgradeMessage() != null)
                    {
                        model.addStatusInfo("host.upgrade.message", host.getUpgradeMessage());
                    }
                }
                else
                {
                    model.addStatusInfo("host.status", I18N.format("host.upgrade.inprogress"));
                    model.addStatusInfo("host.upgrade.status", EnumUtils.toPrettyString(host.getUpgradeState()));

                    int progress = host.getUpgradeProgress();
                    if (progress > 0)
                    {
                        model.addStatusInfo("host.upgrade.progress", Integer.toString(progress) + "%");
                    }
                }
            }
            else if (agent.hasBeenPinged())
            {
                model.addStatusInfo("agent.last.ping", TimeStamps.getPrettyDate(agent.getLastPingTime(), getLocale()));
                model.addStatusInfo("agent.since.last.ping", Long.toString(agent.getSecondsSincePing()) + " seconds");
                if (agent.getPingError() != null)
                {
                    model.addStatusInfo("agent.ping.error", agent.getPingError());
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
                            model.addExecutingInfo(buildResult, node);
                        }
                    }
                }
            }
        }
        else
        {
            model.addStatusInfo("agent.status", agent.getStatus().getPrettyString());
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
