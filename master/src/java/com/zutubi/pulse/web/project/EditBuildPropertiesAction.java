package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.opensymphony.xwork.ActionContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class EditBuildPropertiesAction extends ProjectActionSupport
{
    private long id = -1;
    private Project project;
    private BuildSpecification spec;
    private List<ResourceProperty> properties;
    private static final String PROPERTY_PREFIX = "property.";

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public List<ResourceProperty> getProperties()
    {
        return properties;
    }

    public void validate()
    {
    }

    private void lookupSpec()
    {
        if(id > 0)
        {
            spec = project.getBuildSpecification(id);
        }
        else
        {
            spec = project.getDefaultSpecification();
        }

        if (spec == null)
        {
            addActionError("Request to build unknown build specification id [" + id + "] for project '" + project.getName() + "'");
        }
    }

    public String doInput() throws Exception
    {
        project = lookupProject(projectId);
        if(hasErrors())
        {
            return ERROR;
        }

        lookupSpec();
        if(hasErrors())
        {
            return ERROR;
        }

        properties = new ArrayList<ResourceProperty>(spec.getProperties());
        Collections.sort(properties, new NamedEntityComparator());
        return INPUT;
    }

    public String execute()
    {
        project = lookupProject(projectId);
        if (hasErrors())
        {
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        lookupSpec();
        if(hasErrors())
        {
            return ERROR;
        }

        mapProperties();
        projectManager.save(project);

        projectManager.triggerBuild(project, spec.getName(), new ManualTriggerBuildReason((String)getPrinciple()), null, true);

        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Empty
        }

        return SUCCESS;
    }

    private void mapProperties()
    {
        Map parameters = ActionContext.getContext().getParameters();
        for(Object n: parameters.keySet())
        {
            String name = (String) n;
            if(name.startsWith(PROPERTY_PREFIX))
            {
                String propertyName = name.substring(PROPERTY_PREFIX.length());
                ResourceProperty property = spec.getProperty(propertyName);
                if(property != null)
                {
                    Object value = parameters.get(name);
                    if(value instanceof String)
                    {
                        property.setValue((String) value);
                    }
                    else if(value instanceof String[])
                    {
                        property.setValue(((String[])value)[0]);
                    }
                }
            }
        }
    }
}
