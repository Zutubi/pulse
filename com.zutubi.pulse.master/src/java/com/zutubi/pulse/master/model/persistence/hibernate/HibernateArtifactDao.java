package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.master.model.persistence.ArtifactDao;
import com.zutubi.util.logging.Logger;

public class HibernateArtifactDao extends HibernateEntityDao<StoredArtifact> implements ArtifactDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    @Override
    public Class persistentClass()
    {
        return StoredArtifact.class;
    }
}
