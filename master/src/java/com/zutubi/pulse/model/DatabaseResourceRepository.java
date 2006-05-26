/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class DatabaseResourceRepository implements ResourceRepository
{
    private ResourceRepository parent;
    private ResourceDao resourceDao;

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        if (parent != null && parent.hasResource(name))
        {
            return parent.getResource(name);
        }
        else
        {
            return resourceDao.findByName(name);
        }
    }

    public List<String> getResourceNames()
    {
        // We assume there are not very many resources defined: maybe tens at most
        List<String> names;

        if (parent != null)
        {
            names = parent.getResourceNames();
        }
        else
        {
            names = new LinkedList<String>();
        }

        for (Resource resource : resourceDao.findAll())
        {
            if (!names.contains(resource.getName()))
            {
                names.add(resource.getName());
            }
        }

        return names;
    }

    public void setParent(ResourceRepository parent)
    {
        this.parent = parent;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }

    public void addResource(Resource resource)
    {
        resourceDao.save(resource);
    }
}
