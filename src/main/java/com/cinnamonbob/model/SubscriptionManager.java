package com.cinnamonbob.model;

/**
 *
 *
 */
public interface SubscriptionManager extends EntityManager<Subscription>
{
    public Subscription getSubscription(long id);
}
