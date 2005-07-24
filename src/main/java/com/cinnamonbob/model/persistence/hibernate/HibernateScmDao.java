package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.ScmDao;
import com.cinnamonbob.model.Scm;

/**
 * 
 *
 */
public class HibernateScmDao extends HibernateEntityDao implements ScmDao
{
    public Class persistentClass()
    {
        return Scm.class;
    }
}
