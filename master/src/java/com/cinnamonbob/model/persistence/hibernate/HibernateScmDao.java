package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.persistence.ScmDao;

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
