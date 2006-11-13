package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.persistence.FileArtifactDao;
import com.zutubi.pulse.core.model.StoredFileArtifact;

/**
 */
public class HibernateFileArtifactDao extends HibernateEntityDao<StoredFileArtifact> implements FileArtifactDao
{
    public Class persistentClass()
    {
        return StoredFileArtifact.class;
    }
}
