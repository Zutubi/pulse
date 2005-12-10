package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.BuildSpecification;

/**
 * <class-comment/>
 */
public class HibernateBuildSpecificationDao extends HibernateEntityDao<BuildSpecification> implements BuildSpecificationDao
{
    public Class persistentClass()
    {
        return BuildSpecification.class;
    }
}
