package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;
import com.zutubi.pulse.web.ActionSupport;

/**
 * <class-comment/>
 */
public class DeleteResourceVersionAction extends ResourceActionSupport
{
    public void validate()
    {

    }


    public String execute()
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

        resource.deleteVersion(version);
        getResourceManager().save(resource);
        return SUCCESS;
    }
}
