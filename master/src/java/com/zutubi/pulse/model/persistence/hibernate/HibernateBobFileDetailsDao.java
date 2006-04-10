package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BobFileDetails;
import com.zutubi.pulse.model.persistence.BobFileDetailsDao;

/**
 */
public class HibernateBobFileDetailsDao extends HibernateEntityDao<BobFileDetails> implements BobFileDetailsDao
{
    public Class persistentClass()
    {
        return BobFileDetails.class;
    }
}
