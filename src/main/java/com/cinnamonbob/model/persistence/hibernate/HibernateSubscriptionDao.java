package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.persistence.SubscriptionDao;

/**
 *
 *
 */
public class HibernateSubscriptionDao extends HibernateEntityDao<Subscription> implements SubscriptionDao
{
    public Class persistentClass()
    {
        return Subscription.class;
    }
}
