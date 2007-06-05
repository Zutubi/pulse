package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.CleanupManager;
import com.zutubi.pulse.model.CleanupRule;
import com.zutubi.pulse.model.Project;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This action triggers the specified cleanup rule.
 *
 * This action may take a while to execute, particularly if there are a large number
 * of results that match the cleanup rule. 
 *
 */
public class TriggerCleanupRuleAction extends ProjectActionSupport
{
    private long id;
    private CleanupManager cleanupManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        final Project project = getProjectManager().getProject(projectId);
        if(project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        final CleanupRule rule = project.getCleanupRule(id);

        // run this in the background since it will take some time.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable()
        {
            public void run()
            {
                cleanupManager.cleanupBuilds(project, rule);
            }
        });

        return SUCCESS;
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }
}
