/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.pulse.core.model.EntityComparator;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.queue.*;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.security.Actor;
import com.zutubi.util.Sort;
import com.zutubi.util.adt.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to supply JSON data to the server activity tab.
 */
public class ServerActivityDataAction extends ActionSupport
{
    private ServerActivityModel model;

    private BuildManager buildManager;
    private ConfigurationManager configurationManager;
    private FatController fatController;
    private RecipeQueue recipeQueue;
    private SchedulingController schedulingController;

    public ServerActivityModel getModel()
    {
        return model;
    }

    @Override
    public String execute() throws Exception
    {
        Actor actor = accessManager.getActor();

        model = new ServerActivityModel();
        final boolean isAdmin = accessManager.hasPermission(actor, ServerPermission.ADMINISTER.name(), null);
        model.setBuildQueueTogglePermitted(isAdmin);
        model.setBuildQueueRunning(schedulingController.isRunning());
        model.setStageQueueTogglePermitted(isAdmin);
        model.setStageQueueRunning(recipeQueue.isRunning());
        model.setCancelAllPermitted(isAdmin);

        final List<BuildRequestEvent> queuedBuilds = new LinkedList<BuildRequestEvent>();
        final List<Pair<ActivatedRequest, BuildResult>> activeBuilds = new LinkedList<Pair<ActivatedRequest, BuildResult>>();
        
        SecurityUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                BuildQueueSnapshot snapshot = fatController.snapshotBuildQueue();
                queuedBuilds.addAll(snapshot.getQueuedBuildRequests());

                for (ActivatedRequest activatedRequest : snapshot.getActivatedRequests())
                {
                    BuildController controller = activatedRequest.getController();
                    BuildResult buildResult = buildManager.getBuildResult(controller.getBuildResultId());
                    if (buildResult != null && !buildResult.completed())
                    {
                        activeBuilds.add(new Pair<ActivatedRequest, BuildResult>(activatedRequest, buildResult));
                    }
                }
            }
        });

        Collections.sort(queuedBuilds);
        Collections.reverse(queuedBuilds);
        
        // Ordering by reverse of id makes builds activated first later in the
        // list, giving the effect of builds moving down the page.
        final EntityComparator<BuildResult> buildComparator = new EntityComparator<BuildResult>();
        Collections.sort(activeBuilds, new Sort.InverseComparator<Pair<ActivatedRequest, BuildResult>>(new Comparator<Pair<ActivatedRequest, BuildResult>>()
        {
            public int compare(Pair<ActivatedRequest, BuildResult> o1, Pair<ActivatedRequest, BuildResult> o2)
            {
                return buildComparator.compare(o1.second, o2.second);
            }
        }));
        
        Urls urls = new Urls(configurationManager.getSystemConfig().getContextPathNormalised());
        for (BuildRequestEvent requestEvent: queuedBuilds)
        {
            if (accessManager.hasPermission(actor, AccessManager.ACTION_VIEW, requestEvent))
            {
                boolean cancelPermitted = accessManager.hasPermission(actor, ProjectConfigurationActions.ACTION_CANCEL_BUILD, requestEvent);
                model.addQueued(new ServerActivityModel.QueuedBuildModel(requestEvent, cancelPermitted));
            }
            else
            {
                model.addQueued(new ServerActivityModel.QueuedBuildModel(requestEvent instanceof PersonalBuildRequestEvent));
            }
        }
        for (Pair<ActivatedRequest, BuildResult> active: activeBuilds)
        {
            if (accessManager.hasPermission(actor, AccessManager.ACTION_VIEW, active.second))
            {
                boolean cancelPermitted = accessManager.hasPermission(actor, ProjectConfigurationActions.ACTION_CANCEL_BUILD, active);
                model.addActive(new ServerActivityModel.ActiveBuildModel(active.first, active.second, urls, cancelPermitted));
            }
            else
            {
                model.addActive(new ServerActivityModel.ActiveBuildModel(active.second.isPersonal()));
            }
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }
}
