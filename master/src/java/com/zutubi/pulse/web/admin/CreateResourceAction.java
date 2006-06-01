package com.zutubi.pulse.web.admin;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.web.ActionSupport;

/**
 * 
 *
 */
public class CreateResourceAction extends ResourceActionSupport
{
    public CreateResourceAction()
    {
        resource = new PersistentResource();
    }

    public String doInput()
    {
        // setup any default data.
        lookupSlave();
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

        lookupSlave();
        if (getResourceManager().findBySlaveAndName(slave, resource.getName()) != null)
        {
            addFieldError("resource.name", "A resource with name '" + resource.getName() + "' already exists.");
        }
    }

    public String execute()
    {
        resource.setSlave(slave);
        getResourceManager().save(resource);
        return SUCCESS;
    }
}
