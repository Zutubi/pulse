package com.zutubi.pulse.web.project;

import com.zutubi.pulse.web.admin.ResourceActionSupport;
import com.zutubi.pulse.core.model.ResourceProperty;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.BuildSpecification;
import com.opensymphony.util.TextUtils;

/**
 */
public class EditPropertyAction extends ProjectActionSupport
{
    private long specId;
    private long id;
    private String name;
    private String newName;
    private String newValue;
    private boolean newAddToEnvironment = false;
    private boolean newAddToPath = false;
    private boolean newResolveVariables = false;
    private Project project;
    private BuildSpecification spec;
    private ResourceProperty property;

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpec()
    {
        return spec;
    }

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ResourceProperty getProperty()
    {
        return property;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public boolean getNewAddToEnvironment()
    {
        return newAddToEnvironment;
    }

    public void setNewAddToEnvironment(boolean newAddToEnvironment)
    {
        this.newAddToEnvironment = newAddToEnvironment;
    }

    public boolean getNewAddToPath()
    {
        return newAddToPath;
    }

    public void setNewAddToPath(boolean newAddToPath)
    {
        this.newAddToPath = newAddToPath;
    }

    public boolean getNewResolveVariables()
    {
        return newResolveVariables;
    }

    public void setNewResolveVariables(boolean newResolveVariables)
    {
        this.newResolveVariables = newResolveVariables;
    }

    private void lookupProperty()
    {
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

        property = spec.getProperty(id);
        if(property == null)
        {
            addActionError("Unknown property [" + id + "]");
        }
    }

    public String doInput()
    {
        lookupProperty();
        if(hasErrors())
        {
            return ERROR;
        }

        newName = property.getName();
        newValue = property.getValue();
        newAddToEnvironment = property.getAddToEnvironment();
        newAddToPath = property.getAddToPath();
        newResolveVariables = property.getResolveVariables();

        return INPUT;
    }

    public void validate()
    {
        lookupProperty();

        if (hasErrors())
        {
            return;
        }

        if(!TextUtils.stringSet(newName))
        {
            addFieldError("newName", "name is required");
            return;
        }

        if(!property.getName().equals(newName))
        {
            // The name has changed, ensure that the new name does not clash.
            if (spec.hasProperty(newName))
            {
                addFieldError("newName", "this specification already contains a property with name '" + newName + "'");
            }
        }
    }

    public String execute()
    {
        property.setName(newName);
        property.setValue(newValue);
        property.setAddToEnvironment(newAddToEnvironment);
        property.setAddToPath(newAddToPath);
        property.setResolveVariables(newResolveVariables);
        projectManager.save(project);
        return SUCCESS;
    }
}
