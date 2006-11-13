package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.model.ResourceProperty;

/**
 * <class-comment/>
 */
public class DeletePropertyAction extends ResourceActionSupport
{
    private String name;

    public void setName(String name)
    {
        this.name = name;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        lookupResource();
        if(hasErrors())
        {
            return;
        }

        lookupVersion();
        if(hasErrors())
        {
            return;
        }

        ResourceProperty property;
        if (versionId == null)
        {
            property = resource.getProperty(name);
        }
        else
        {
            property = version.getProperty(name);
        }

        if (property == null)
        {
            addActionError("Unknown property '" + name + "'");
        }
    }

    public String execute()
    {
        if (version == null)
        {
            resource.deleteProperty(name);
        }
        else
        {
            version.deleteProperty(name);
        }

        getResourceManager().save(resource);
        return SUCCESS;
    }
}
