package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.BobFileDetails;
import com.cinnamonbob.model.persistence.BobFileDetailsDao;

/**
 */
public class HibernateBobFileDetailsDao extends HibernateEntityDao<BobFileDetails> implements BobFileDetailsDao
{
    public Class persistentClass()
    {
        return BobFileDetails.class;
    }
}
