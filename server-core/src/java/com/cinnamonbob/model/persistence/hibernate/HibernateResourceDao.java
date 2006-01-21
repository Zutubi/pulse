package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.model.persistence.ResourceDao;

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
