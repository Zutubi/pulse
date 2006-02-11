package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.BuildSpecificationDao;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Preparable;

/**
 */
public class EditBuildSpecificationAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private long projectId;
    private Project project;
    private BuildSpecification spec;
    private BuildSpecificationDao buildSpecificationDao;
    private String recipe;
    private boolean timeoutEnabled;
    private int timeout = 60;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

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
        if (project == null)
        {
            project = getProjectManager().getProject(projectId);
        }

        return project;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public boolean isTimeoutEnabled()
    {
        return timeoutEnabled;
    }

    public void setTimeoutEnabled(boolean timeoutEnabled)
    {
        this.timeoutEnabled = timeoutEnabled;
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public boolean checkSpec()
    {
        if (spec == null)
        {
            addActionError("Unknown build specification [" + getId() + "]");
            return true;
        }

        return false;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        spec = buildSpecificationDao.findById(id);
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (checkSpec())
        {
            return;
        }

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        BuildSpecification specOfName = project.getBuildSpecification(spec.getName());
        if (specOfName != null && specOfName.getId() != id)
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

    public String doInput()
    {
        if (checkSpec())
        {
            return ERROR;
        }

        timeoutEnabled = spec.getTimeout() != BuildSpecification.TIMEOUT_NEVER;
        if (timeoutEnabled)
        {
            timeout = spec.getTimeout();
        }
        else
        {
            timeout = 60;
        }

        return INPUT;
    }

    public String execute()
    {
        if (!TextUtils.stringSet(recipe))
        {
            recipe = null;
        }

        spec.getRoot().getChildren().get(0).getStage().setRecipe(recipe);

        if (timeoutEnabled)
        {
            spec.setTimeout(timeout);
        }
        else
        {
            spec.setTimeout(BuildSpecification.TIMEOUT_NEVER);
        }

        buildSpecificationDao.save(spec);

        return SUCCESS;
    }

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }
}
