package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.build.queue.RecipeQueue;
import com.zutubi.pulse.master.build.queue.ActivatedRequest;
import com.zutubi.pulse.master.build.queue.BuildQueueSnapshot;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import static com.zutubi.tove.security.AccessManager.ACTION_VIEW;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Action to show the build and recipe queues.
 */
public class ViewServerQueuesAction extends ActionSupport
{
    private List<BuildRequestEvent> buildQueue;
    private List<BuildResult> executingBuilds;
    private List<RecipeAssignmentRequest> recipeQueueSnapshot;

    private FatController fatController;
    private RecipeQueue recipeQueue;
    private BuildManager buildManager;

    public List<BuildRequestEvent> getBuildQueue()
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
        // We snapshot the queues with full privileges so we can show a more
        // complete picture to the user by replacing entries they don't have
        // the authority to view with placeholders.
        snapshotQueuesAsSystem();
        sortBuilds();
        nullOutUnviewableEntries();

        return SUCCESS;
    }

    private void snapshotQueuesAsSystem()
    {
        AcegiUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                recipeQueueSnapshot = recipeQueue.takeSnapshot();

                buildQueue = new LinkedList<BuildRequestEvent>();
                executingBuilds = new LinkedList<BuildResult>();

                BuildQueueSnapshot snapshot = fatController.snapshotBuildQueue();
                buildQueue.addAll(snapshot.getQueuedBuildRequests());

                for (ActivatedRequest activatedRequest: snapshot.getActivatedRequests())
                {
                    BuildController controller = activatedRequest.getController();
                    BuildResult buildResult = buildManager.getBuildResult(controller.getBuildResultId());
                    if (buildResult != null && !buildResult.completed())
                    {
                        executingBuilds.add(buildResult);
                    }
                }
            }
        });
    }

    private void nullOutUnviewableEntries()
    {
        for (int i = 0; i < buildQueue.size(); i++)
        {
            if (!accessManager.hasPermission(ACTION_VIEW, buildQueue.get(i).getOwner()))
            {
                buildQueue.set(i, null);
            }
        }

        for (int i = 0; i < executingBuilds.size(); i++)
        {
            if (!accessManager.hasPermission(ACTION_VIEW, executingBuilds.get(i).getOwner()))
            {
                executingBuilds.set(i, null);
            }
        }

        for (int i = 0; i < recipeQueueSnapshot.size(); i++)
        {
            if (!accessManager.hasPermission(ACTION_VIEW, recipeQueueSnapshot.get(i).getProject()))
            {
                recipeQueueSnapshot.set(i, null);
            }

        }
    }

    private void sortBuilds()
    {
        Collections.sort(buildQueue);

        Collections.sort(executingBuilds, new Comparator<BuildResult>()
        {
            public int compare(BuildResult o1, BuildResult o2)
            {
                return (int) (o1.getStamps().getStartTime() - o2.getStamps().getStartTime());
            }
        });
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
