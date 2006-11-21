package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.*;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.util.List;
import java.util.LinkedList;
import java.io.ByteArrayInputStream;

/**
 */
public class CreateBuildSpecificationAction extends BuildSpecificationActionSupport
{
    private BuildSpecification spec = new BuildSpecification();
    private int timeout = 60;
    private boolean timeoutEnabled = false;
    private boolean prompt = false;
    private long specId;

    public BuildSpecification getSpec()
    {
        return spec;
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

    public long getSpecId()
    {
        return specId;
    }

    public boolean isTimeoutEnabled()
    {
        return timeoutEnabled;
    }

    public void setTimeoutEnabled(boolean timeoutEnabled)
    {
        this.timeoutEnabled = timeoutEnabled;
    }

    public boolean isPrompt()
    {
        return prompt;
    }

    public void setPrompt(boolean prompt)
    {
        this.prompt = prompt;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        return INPUT;
    }

    public void validate()
    {
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        if (TextUtils.stringSet(spec.getName()) && project.getBuildSpecification(spec.getName()) != null)
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

        lookupAgent();
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

        spec.setPrompt(prompt);
        
        BuildSpecificationNode node = new BuildSpecificationNode(stage);
        spec.getRoot().addChild(node);
        addFieldsToStage();

        project.addBuildSpecification(spec);
        getProjectManager().save(project);
        specId = spec.getId();
        return SUCCESS;
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }
}
