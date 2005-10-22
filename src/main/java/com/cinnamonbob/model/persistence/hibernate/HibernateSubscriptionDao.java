package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.SubscriptionDao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 *
 *
 */
public class HibernateSubscriptionDao extends HibernateEntityDao<Subscription> implements SubscriptionDao
{
    @Override
    public Class persistentClass()
    {
        return Subscription.class;
    }

    public List<Subscription> findByProject(final Project project)
    {
        return (List<Subscription>)findByNamedQuery("subscription.findByProject", "project", project);
    }
}
