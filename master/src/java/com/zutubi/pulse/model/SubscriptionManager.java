package com.zutubi.pulse.model;

import java.util.List;

/**
 *
 *
 */
public interface SubscriptionManager extends EntityManager<Subscription>
{
    public Subscription getSubscription(long id);

    public List<Subscription> getSubscriptions(Project project);

    void deleteAllSubscriptions(Project project);
}
