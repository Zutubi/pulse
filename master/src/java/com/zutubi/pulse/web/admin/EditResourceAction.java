package com.zutubi.pulse.web.admin;

import com.opensymphony.util.TextUtils;

/**
 */
public class EditResourceAction extends ResourceActionSupport
{
    private String newName;

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public String doInput()
    {
        lookupResource();
        if(hasErrors())
        {
            return ERROR;
        }

        newName = resource.getName();
        return INPUT;
    }

    public void validate()
    {
        lookupResource();
        if(hasErrors())
        {
            return;
        }

        if(!TextUtils.stringSet(newName))
        {
            addFieldError("newName", "name is required");
            return;
        }

        if(!resource.getName().equals(newName))
        {
            // The name has changed, ensure that the new name does not clash.
            if(getResourceManager().findBySlaveAndName(resource.getSlave(), newName) != null)
            {
                addFieldError("newName", "this agent already has a resource with name '" + newName + "'");
            }
        }
    }

    public String execute()
    {
        getResourceManager().renameResource(resource, newName);
        return SUCCESS;
    }
}
