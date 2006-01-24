package com.cinnamonbob.web.project;

import com.cinnamonbob.model.AgeBuildResultCleanupPolicy;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class EditCleanupPolicyAction extends ProjectActionSupport
{
    private long projectId;
    private AgeBuildResultCleanupPolicy policy;
    private Project project;
    private boolean enableWorkDirCleanup;
    private boolean enableResultCleanup;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public AgeBuildResultCleanupPolicy getPolicy()
    {
        return policy;
    }

    public void setPolicy(AgeBuildResultCleanupPolicy policy)
    {
        this.policy = policy;
    }

    public boolean getEnableWorkDirCleanup()
    {
        return enableWorkDirCleanup;
    }

    public void setEnableWorkDirCleanup(boolean enableWorkDirCleanup)
    {
        this.enableWorkDirCleanup = enableWorkDirCleanup;
    }

    public boolean getEnableResultCleanup()
    {
        return enableResultCleanup;
    }

    public void setEnableResultCleanup(boolean enableResultCleanup)
    {
        this.enableResultCleanup = enableResultCleanup;
    }

    public String doDefault()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project '" + projectId + "'");
            return ERROR;
        }

        policy = project.getCleanupPolicy();
        enableWorkDirCleanup = policy.getWorkDirDays() != AgeBuildResultCleanupPolicy.NEVER_CLEAN;
        enableResultCleanup = policy.getResultDays() != AgeBuildResultCleanupPolicy.NEVER_CLEAN;

        return SUCCESS;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project '" + projectId + "'");
            return;
        }

        if (enableWorkDirCleanup)
        {
            if (policy.getWorkDirDays() <= 0)
            {
                addFieldError("policy.workDirDays", "working directory days must be a positive value");
            }
        }

        if (enableResultCleanup)
        {
            if (policy.getResultDays() <= 0)
            {
                addFieldError("policy.resultDays", "result days must be a positive value");
            }
        }

        if (!hasErrors() && enableWorkDirCleanup && enableResultCleanup)
        {
            if (policy.getWorkDirDays() > policy.getResultDays())
            {
                addActionError("It is not meaningful to clean up working directories less frequently than results");
            }
        }
    }

    public String execute()
    {
        if (!enableWorkDirCleanup)
        {
            policy.setWorkDirDays(AgeBuildResultCleanupPolicy.NEVER_CLEAN);
        }

        if (!enableResultCleanup)
        {
            policy.setResultDays(AgeBuildResultCleanupPolicy.NEVER_CLEAN);
        }

        project.setCleanupPolicy(policy);
        getProjectManager().save(project);
        return SUCCESS;
    }

}
