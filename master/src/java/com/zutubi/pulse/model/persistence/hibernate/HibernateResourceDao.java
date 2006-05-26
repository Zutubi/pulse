/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;

/**
 */
public class HibernateResourceDao extends HibernateEntityDao<Resource> implements ResourceDao
{
    public Class persistentClass()
    {
        return Resource.class;
    }

    public Resource findByName(String name)
    {
        return (Resource) findUniqueByNamedQuery("findResourceByName", "name", name, true);
    }
}
