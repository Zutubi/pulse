package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;

/**
 */
public class CreatePropertyAction extends ProjectActionSupport
{
    private ResourceProperty property = new ResourceProperty();
    private long specId;
    private Project project;
    private BuildSpecification spec;

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public ResourceProperty getProperty()
    {
        return property;
    }

    public void setProperty(ResourceProperty property)
    {
        this.property = property;
    }

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }


    // The direct use of value is purely to get around the fact that Property
    // implements Reference, meaning its getValue() returns Object, which
    // appears to disagree with WebWork.
    public String getValue()
    {
        return property.getValue();
    }

    public void setValue(String value)
    {
        property.setValue(value);
    }

    public String doInput() throws Exception
    {
        project = lookupProject(projectId);
        if(project != null)
        {
            spec = project.getBuildSpecification(specId);
        }
        
        return INPUT;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }

        project = lookupProject(projectId);
        if(hasErrors())
        {
            return;
        }

        spec = project.getBuildSpecification(specId);
        if(spec == null)
        {
            addActionError("Unknown build specification [" + specId + "]");
            return;
        }

        if (spec.hasProperty(property.getName()))
        {
            addFieldError("property.name", "This specification already contains a property with name '" + property.getName() + "'");
        }
    }

    public String execute()
    {
        spec.addProperty(property);
        projectManager.save(project);
        return SUCCESS;
    }
}
