package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.persistence.ContactPointDao;

/**
 *
 * 
 */
public class HibernateContactPointDao extends HibernateEntityDao<ContactPoint> implements ContactPointDao
{

    public Class persistentClass()
    {
        return ContactPoint.class;
    }
}
