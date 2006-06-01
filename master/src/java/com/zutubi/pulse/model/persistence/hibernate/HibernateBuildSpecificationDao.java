package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;

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
