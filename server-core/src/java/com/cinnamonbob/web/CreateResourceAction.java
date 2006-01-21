package com.cinnamonbob.web;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

/**
 * 
 *
 */
public class CreateResourceAction extends ActionSupport
{
    private Resource resource = new Resource();
    private ResourceDao resourceDao;

    public Resource getResource()
    {
        return resource;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (resourceDao.findByName(resource.getName()) != null)
        {
            addFieldError("resource.name", "A resource with name '" + resource.getName() + "' already exists.");
        }
    }

    public String execute()
    {
        resourceDao.save(resource);
        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
