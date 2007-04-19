package com.zutubi.pulse.web.admin;

import com.opensymphony.util.TextUtils;
import com.zutubi.util.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 */
public class EditResourceAction extends ResourceActionSupport
{
    private String newName;
    private String defaultVersion;

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    public String getDefaultVersion()
    {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion)
    {
        this.defaultVersion = defaultVersion;
    }

    public List<String> getVersions()
    {
        Set<String> versions = resource.getVersions().keySet();
        List<String> result = new ArrayList<String>(versions.size() + 1);
        result.add("");
        result.addAll(versions);
        Collections.sort(result, new Sort.StringComparator());
        return result;
    }

    public String doInput()
    {
        lookupResource();
        if(hasErrors())
        {
            return ERROR;
        }

        newName = resource.getName();
        defaultVersion = resource.getDefaultVersion();

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
        getResourceManager().editResource(resource, newName, defaultVersion);
        return SUCCESS;
    }
}
