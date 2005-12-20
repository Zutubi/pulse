package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.persistence.BuildSpecificationDao;

/**
 * <class-comment/>
 */
public class HibernateBuildSpecificationDao extends HibernateEntityDao<BuildSpecification> implements BuildSpecificationDao
{
    public Class persistentClass()
    {
        return BuildSpecification.class;
    }

    public BuildSpecification findByName(String name)
    {
        return (BuildSpecification) findUniqueByNamedQuery("findBuildSpecificationByName", "name", name, true);
    }
}
