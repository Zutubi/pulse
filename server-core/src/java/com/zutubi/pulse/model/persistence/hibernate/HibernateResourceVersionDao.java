package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.ResourceVersion;
import com.cinnamonbob.model.persistence.ResourceVersionDao;

/**
 */
public class HibernateResourceVersionDao extends HibernateEntityDao<ResourceVersion> implements ResourceVersionDao
{
    public Class persistentClass()
    {
        return ResourceVersion.class;
    }
}
