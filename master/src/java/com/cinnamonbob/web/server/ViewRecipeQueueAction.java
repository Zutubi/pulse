package com.cinnamonbob.web.server;

import com.cinnamonbob.FatController;
import com.cinnamonbob.RecipeDispatchRequest;
import com.cinnamonbob.RecipeQueue;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.web.ActionSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ViewRecipeQueueAction extends ActionSupport
{
    private FatController fatController;
    private RecipeQueue recipeQueue;
    private Map<Project, List<BuildRequestEvent>> projectQueue;
    private List<RecipeDispatchRequest> recipeQueueSnapshot;

    public Map<Project, List<BuildRequestEvent>> getProjectQueue()
    {
        return projectQueue;
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

    public String execute() throws Exception
    {
        recipeQueueSnapshot = recipeQueue.takeSnapshot();
        projectQueue = fatController.snapshotProjectQueue();
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

}
