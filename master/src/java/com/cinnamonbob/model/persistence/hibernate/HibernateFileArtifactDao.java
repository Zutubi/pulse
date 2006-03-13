package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.FileArtifactDao;
import com.cinnamonbob.core.model.StoredFileArtifact;

/**
 */
public class HibernateFileArtifactDao extends HibernateEntityDao<StoredFileArtifact> implements FileArtifactDao
{
    public Class persistentClass()
    {
        return StoredFileArtifact.class;
    }
}
