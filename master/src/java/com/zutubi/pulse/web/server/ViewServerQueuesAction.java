package com.zutubi.pulse.web.server;

import com.zutubi.pulse.FatController;
import com.zutubi.pulse.RecipeDispatchRequest;
import com.zutubi.pulse.RecipeQueue;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class ViewServerQueuesAction extends ActionSupport
{
    private List<AbstractBuildRequestEvent> buildQueue;
    private List<BuildResult> executingBuilds;
    private List<RecipeDispatchRequest> recipeQueueSnapshot;
    private FatController fatController;
    private RecipeQueue recipeQueue;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;

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

    public List<RecipeDispatchRequest> getRecipeQueueSnapshot()
    {
        return recipeQueueSnapshot;
    }

    public boolean canCancel(BuildResult build)
    {
        Object principle = getPrinciple();
        if(principle != null && principle instanceof String)
        {
            return buildManager.canCancel(build, userManager.getUser((String)principle));
        }

        return false;
    }

    public String execute() throws Exception
    {
        recipeQueueSnapshot = recipeQueue.takeSnapshot();

        buildQueue = new LinkedList<AbstractBuildRequestEvent>();
        executingBuilds = new LinkedList<BuildResult>();

        Map<Object, List<AbstractBuildRequestEvent>> builds = fatController.snapshotBuildQueue();
        for(Object entity: builds.keySet())
        {
            List<AbstractBuildRequestEvent> events = builds.get(entity);
            if(events.size() > 0)
            {
                AbstractBuildRequestEvent active = events.get(0);
                BuildResult result;
                if(active.isPersonal())
                {
                    result = buildManager.getLatestBuildResult((User) active.getOwner());
                }
                else
                {
                    ProjectConfiguration projectConfig = (ProjectConfiguration) active.getOwner();
                    Project project = projectManager.getProject(projectConfig.getId());
                    result = buildManager.getLatestBuildResult(project);
                }

                if(result != null && !result.completed())
                {
                    executingBuilds.add(result);
                }

                if(events.size() > 1)
                {
                    buildQueue.addAll(events.subList(1, events.size()));
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
                return (int) (o1.getStamps().getStartTime() - o2.getStamps().getEndTime());
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

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
