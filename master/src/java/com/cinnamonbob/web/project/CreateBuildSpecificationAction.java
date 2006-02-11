package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;
import com.opensymphony.util.TextUtils;

/**
 */
public class CreateBuildSpecificationAction extends ProjectActionSupport
{
    private long projectId;
    private Project project;
    private BuildSpecification spec = new BuildSpecification();
    private String recipe;
    private int timeout = 60;
    private boolean timeoutEnabled = false;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public void setSpec(BuildSpecification spec)
    {
        this.spec = spec;
    }

    public boolean isTimeoutEnabled()
    {
        return timeoutEnabled;
    }

    public void setTimeoutEnabled(boolean timeoutEnabled)
    {
        this.timeoutEnabled = timeoutEnabled;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        return INPUT;
    }

    public void validate()
    {
        project = getProjectManager().getProject(projectId);

        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        if (project.getBuildSpecification(spec.getName()) != null)
        {
            addFieldError("spec.name", "A build specification with name '" + spec.getName() + "' already exists in this project.");
        }

        if (timeoutEnabled)
        {
            if (timeout <= 0)
            {
                addFieldError("timeout", "Timeout must be a positive value");
            }
        }
    }

    public String execute()
    {
        if (!TextUtils.stringSet(recipe))
        {
            recipe = null;
        }

        BuildSpecificationNode node = new BuildSpecificationNode(new BuildStage(new MasterBuildHostRequirements(), recipe));
        spec.getRoot().addChild(node);

        if (timeoutEnabled)
        {
            spec.setTimeout(timeout);
        }
        else
        {
            spec.setTimeout(BuildSpecification.TIMEOUT_NEVER);
        }

        project.addBuildSpecification(spec);
        getProjectManager().save(project);
        return SUCCESS;
    }
}
