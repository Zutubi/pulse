package com.cinnamonbob.model.persistence.hibernate;

import java.util.logging.Logger;

import com.cinnamonbob.model.StoredArtifact;
import com.cinnamonbob.model.persistence.ArtifactDao;

public class HibernateArtifactDao extends HibernateEntityDao<StoredArtifact> implements ArtifactDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

    @Override
    public Class persistentClass()
    {
        return StoredArtifact.class;
    }    
}
