package com.zutubi.pulse.web.admin;

import com.opensymphony.util.TextUtils;

/**
 */
public class EditResourceVersionAction extends ResourceActionSupport
{
    private String newValue;

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String doInput()
    {
        lookupResource();
        if(hasErrors())
        {
            return ERROR;
        }

        lookupVersion(true);
        if(hasErrors())
        {
            return ERROR;
        }

        newValue = version.getValue();
        return INPUT;
    }

    public void validate()
    {
        lookupResource();
        if(hasErrors())
        {
            return;
        }

        lookupVersion(true);
        if(hasErrors())
        {
            return;
        }

        if(!TextUtils.stringSet(newValue))
        {
            addFieldError("newValue", "version is required");
            return;
        }

        if(!version.getValue().equals(newValue))
        {
            // The value has changed, ensure that the new value does not clash.
            if(resource.getVersion(newValue) != null)
            {
                addFieldError("newValue", "this resource already has a version '" + newValue + "'");
            }
        }
    }

    public String execute()
    {
        getResourceManager().renameResourceVersion(resource, version.getValue(), newValue);
        return SUCCESS;
    }
}
