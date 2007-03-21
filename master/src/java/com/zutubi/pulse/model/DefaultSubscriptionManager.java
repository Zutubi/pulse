package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.SubscriptionDao;

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
        List<Subscription> subscriptions = subscriptionDao.findByProject(project);
        subscriptions.addAll(subscriptionDao.findByNoProject());
        return subscriptions;
    }

    public void deleteAllSubscriptions(Project project)
    {
        List<Subscription> suscriptions = subscriptionDao.findByProject(project);
        for(Subscription s: suscriptions)
        {
            if(s instanceof ProjectBuildSubscription)
            {
                ProjectBuildSubscription pbs = (ProjectBuildSubscription) s;
                List<Project> projects = pbs.getProjects();
                projects.remove(project);
                if(projects.size() == 0)
                {
                    // This subscription is no longer useful.
                    delete(s);
                }
            }
        }
    }

    public void delete(ProjectBuildCondition condition)
    {
        subscriptionDao.delete(condition);
    }
}
