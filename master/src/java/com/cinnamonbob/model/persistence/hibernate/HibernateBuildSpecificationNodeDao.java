package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.BuildSpecificationNode;
import com.cinnamonbob.model.persistence.BuildSpecificationNodeDao;

/**
 */
public class HibernateBuildSpecificationNodeDao extends HibernateEntityDao<BuildSpecificationNode> implements BuildSpecificationNodeDao
{
    public Class persistentClass()
    {
        return BuildSpecificationNode.class;
    }
}
