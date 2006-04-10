package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.model.persistence.ScmDao;

/**
 * 
 *
 */
public class HibernateScmDao extends HibernateEntityDao<Scm> implements ScmDao
{
    @Override
    public Class persistentClass()
    {
        return Scm.class;
    }
}
