package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core2.BuildResult;
import com.cinnamonbob.model.persistence.BuildResultDao;

public class HibernateBuildResultDao extends HibernateEntityDao implements BuildResultDao
{
    @Override
    public Class persistentClass()
    {
        return BuildResult.class;
    }
}
