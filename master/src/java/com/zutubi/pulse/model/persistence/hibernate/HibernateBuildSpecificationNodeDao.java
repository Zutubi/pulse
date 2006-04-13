/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.persistence.BuildSpecificationNodeDao;

/**
 */
public class HibernateBuildSpecificationNodeDao extends HibernateEntityDao<BuildSpecificationNode> implements BuildSpecificationNodeDao
{
    public Class persistentClass()
    {
        return BuildSpecificationNode.class;
    }
}
