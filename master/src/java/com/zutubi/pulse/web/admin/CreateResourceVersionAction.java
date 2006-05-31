/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.web.ActionSupport;

/**
 * Used to add a new version to a resource.
 */
public class CreateResourceVersionAction extends ResourceActionSupport
{
    public CreateResourceVersionAction()
    {
        version = new ResourceVersion();
    }

    public String doInput()
    {
        lookupResource();
        if(hasErrors())
        {
            return ERROR;
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

        lookupResource();
        if(hasErrors())
        {
            return;
        }

        if (resource.hasVersion(version.getValue()))
        {
            addFieldError("resourceVersion.value", "this resource already has a version '" + version.getValue() + "'");
        }
    }

    public String execute()
    {
        resource.add(version);
        getResourceManager().save(resource);
        return SUCCESS;
    }
}
