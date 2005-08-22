package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.SubscriptionDao;

import java.util.List;

/**
 *
 *
 */
public class DefaultSubscriptionManager implements SubscriptionManager
{
    private SubscriptionDao subscriptionDao;

    public void setSubscriptionDao(SubscriptionDao dao)
    {
        this.subscriptionDao = dao;
    }

    public void save(Subscription subscription)
    {
        subscriptionDao.save(subscription);
    }

    public void delete(Subscription subscription)
    {
        // un do associations.
        subscription.getContactPoint().remove(subscription);
        subscriptionDao.delete(subscription);
    }

    public Subscription getSubscription(long id)
    {
        return subscriptionDao.findById(id);
    }

    public List<Subscription> getSubscriptions(Project project)
    {
        return subscriptionDao.findByProject(project);
    }
}
