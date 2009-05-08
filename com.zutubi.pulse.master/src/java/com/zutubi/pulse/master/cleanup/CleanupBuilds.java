package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.cleanup.requests.ProjectCleanupRequest;
import com.zutubi.util.bean.ObjectFactory;

import java.util.List;
import java.util.LinkedList;

/**
 * A scheduled task that when triggered will generate a cleanup
 * request for all of the projects.
 */
public class CleanupBuilds implements Task
{
    private ProjectManager projectManager;
    private CleanupManager cleanupManager;
    private ObjectFactory objectFactory;

    public void execute(TaskExecutionContext context)
    {
        List<CleanupRequest> requests = new LinkedList<CleanupRequest>();
        List<Project> projects = projectManager.getProjects(false);
        for (Project project : projects)
        {
            requests.add(createRequest(project));
        }
        if (requests.size() > 0)
        {
            cleanupManager.process(requests);
        }
    }

    private ProjectCleanupRequest createRequest(Project project)
    {
        return objectFactory.buildBean(ProjectCleanupRequest.class, new Class[]{Project.class}, new Object[]{project});
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}