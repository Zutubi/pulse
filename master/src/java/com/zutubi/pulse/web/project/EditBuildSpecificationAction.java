package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.xwork.interceptor.Preparable;
import com.opensymphony.util.TextUtils;

import java.util.Arrays;
import java.util.List;

/**
 */
public class EditBuildSpecificationAction extends BuildSpecificationActionSupport implements Preparable
{
    private long id;
    private Project project;
    private BuildSpecification spec;
    private BuildSpecificationDao buildSpecificationDao;
    private boolean timeoutEnabled;
    private int timeout = 60;
    private static final List<String> PREPARE_PARAMS = Arrays.asList("id", "projectId");

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
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

    public List<String> getPrepareParameterNames()
    {
        return PREPARE_PARAMS;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(projectId);
        spec = buildSpecificationDao.findById(id);
    }

    public void validate()
    {
        if (checkSpec())
        {
            return;
        }

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        if (TextUtils.stringSet(spec.getName()))
        {
            BuildSpecification specOfName = project.getBuildSpecification(spec.getName());
            if (specOfName != null && specOfName.getId() != id)
            {
                addFieldError("spec.name", "A build specification with name '" + spec.getName() + "' already exists in this project.");
            }
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
