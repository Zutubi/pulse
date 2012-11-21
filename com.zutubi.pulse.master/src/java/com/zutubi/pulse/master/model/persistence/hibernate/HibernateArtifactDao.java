package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.model.persistence.ArtifactDao;

/**
 * Hibernate-specific implementation of {@link ArtifactDao}.
 */
public class HibernateArtifactDao extends HibernateEntityDao<StoredArtifact> implements ArtifactDao
{
    @Override
    public Class<StoredArtifact> persistentClass()
    {
        return StoredArtifact.class;
    }
}
