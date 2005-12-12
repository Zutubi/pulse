package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.core.model.StoredArtifact;
import com.cinnamonbob.model.persistence.ArtifactDao;

import com.cinnamonbob.util.logging.Logger;

public class HibernateArtifactDao extends HibernateEntityDao<StoredArtifact> implements ArtifactDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

    @Override
    public Class persistentClass()
    {
        return StoredArtifact.class;
    }
}
