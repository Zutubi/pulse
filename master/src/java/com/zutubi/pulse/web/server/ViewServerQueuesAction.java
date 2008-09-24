package com.zutubi.pulse.web.server;

import com.zutubi.pulse.*;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to show the build and recipe queues.
 */
public class ViewServerQueuesAction extends ActionSupport
{
    private List<AbstractBuildRequestEvent> buildQueue;
    private List<BuildResult> executingBuilds;
    private List<RecipeAssignmentRequest> recipeQueueSnapshot;
    private FatController fatController;
    private RecipeQueue recipeQueue;
    private BuildManager buildManager;

    public List<AbstractBuildRequestEvent> getBuildQueue()
    {
        return buildQueue;
    }

    public List<BuildResult> getExecutingBuilds()
    {
        return executingBuilds;
    }

    public boolean getRecipeQueueRunning()
    {
        return recipeQueue.isRunning();
    }

    public List<RecipeAssignmentRequest> getRecipeQueueSnapshot()
    {
        return recipeQueueSnapshot;
    }

    public boolean canCancel(Object resource)
    {
        return accessManager.hasPermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, resource);
    }

    public String execute() throws Exception
    {
        recipeQueueSnapshot = recipeQueue.takeSnapshot();

        buildQueue = new LinkedList<AbstractBuildRequestEvent>();
        executingBuilds = new LinkedList<BuildResult>();

        BuildQueue.Snapshot snapshot = fatController.snapshotBuildQueue();
        for (List<AbstractBuildRequestEvent> queuedForEntity: snapshot.getQueuedBuilds().values())
        {
            buildQueue.addAll(queuedForEntity);
        }

        for (List<EntityBuildQueue.ActiveBuild> activeForEntity: snapshot.getActiveBuilds().values())
        {
            for (EntityBuildQueue.ActiveBuild activeBuild: activeForEntity)
            {
                BuildResult buildResult = buildManager.getBuildResult(activeBuild.getController().getBuildId());
                if (buildResult != null && !buildResult.completed())
                {
                    executingBuilds.add(buildResult);
                }
            }
        }

        Collections.sort(buildQueue, new Comparator<AbstractBuildRequestEvent>()
        {
            public int compare(AbstractBuildRequestEvent o1, AbstractBuildRequestEvent o2)
            {
                return (int) (o1.getQueued() - o2.getQueued());
            }
        });

        Collections.sort(executingBuilds, new Comparator<BuildResult>()
        {
            public int compare(BuildResult o1, BuildResult o2)
            {
                return (int) (o1.getStamps().getStartTime() - o2.getStamps().getStartTime());
            }
        });

        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setRecipeQueue(RecipeQueue queue)
    {
        this.recipeQueue = queue;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
