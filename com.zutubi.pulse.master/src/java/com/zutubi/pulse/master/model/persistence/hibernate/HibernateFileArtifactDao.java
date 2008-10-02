package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.model.persistence.FileArtifactDao;

/**
 */
public class HibernateFileArtifactDao extends HibernateEntityDao<StoredFileArtifact> implements FileArtifactDao
{
    public Class persistentClass()
    {
        return StoredFileArtifact.class;
    }
}
