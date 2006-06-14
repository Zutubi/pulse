package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceVersionDao;

/**
 */
public class HibernateResourceVersionDao extends HibernateEntityDao<ResourceVersion> implements ResourceVersionDao
{
    public Class persistentClass()
    {
        return ResourceVersion.class;
    }
}
