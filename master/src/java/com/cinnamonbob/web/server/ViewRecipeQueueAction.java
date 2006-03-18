package com.cinnamonbob.web.server;

import com.cinnamonbob.FatController;
import com.cinnamonbob.RecipeDispatchRequest;
import com.cinnamonbob.RecipeQueue;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.web.ActionSupport;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ViewRecipeQueueAction extends ActionSupport
{
    private FatController fatController;
    private RecipeQueue recipeQueue;
    private BuildManager buildManager;
    private Map<Project, List<BuildRequestEvent>> projectQueue;
    private Map<Project, BuildResult> latestBuilds;
    private List<RecipeDispatchRequest> recipeQueueSnapshot;

    public Map<Project, List<BuildRequestEvent>> getProjectQueue()
    {
        return projectQueue;
    }

    public boolean getRecipeQueueRunning()
    {
        return recipeQueue.isRunning();
    }

    public List<RecipeDispatchRequest> getRecipeQueueSnapshot()
    {
        return recipeQueueSnapshot;
    }

    public boolean hasActiveBuilds()
    {
        return hasQueueSized(1);
    }

    public boolean hasActiveBuild(Project project)
    {
        return projectQueue.containsKey(project) && projectQueue.get(project).size() > 0;
    }

    public boolean hasQueuedRequests()
    {
        return hasQueueSized(2);
    }

    private boolean hasQueueSized(int size)
    {
        for (List<BuildRequestEvent> l : projectQueue.values())
        {
            if (l.size() >= size)
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasQueuedRequests(Project project)
    {
        return projectQueue.containsKey(project) && projectQueue.get(project).size() > 1;
    }

    public List<BuildRequestEvent> getQueuedRequests(Project project)
    {
        List<BuildRequestEvent> result = new LinkedList<BuildRequestEvent>(projectQueue.get(project));
        result.remove(0);
        return result;
    }

    public boolean hasBuild(Project p)
    {
        return latestBuilds.containsKey(p);
    }

    public BuildResult getBuild(Project p)
    {
        return latestBuilds.get(p);
    }
    
    public String execute() throws Exception
    {
        recipeQueueSnapshot = recipeQueue.takeSnapshot();
        projectQueue = fatController.snapshotProjectQueue();
        latestBuilds = new HashMap<Project, BuildResult>(projectQueue.size());
        for(Project p: projectQueue.keySet())
        {
            BuildResult result = buildManager.getLatestBuildResult(p);
            if(result.inProgress())
            {
                latestBuilds.put(p, result);
            }
        }
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
