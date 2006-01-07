package com.cinnamonbob;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.BuildCompletedEvent;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.SubscriptionManager;

import java.util.List;

/**
 */
public class ResultNotifier implements EventListener
{
    private EventManager eventManager;
    private SubscriptionManager subscriptionManager;

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        this.eventManager.register(this);
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
    }

    public void handleEvent(Event evt)
    {
        BuildCompletedEvent event = (BuildCompletedEvent) evt;
        BuildResult buildResult = event.getResult();

        List<Subscription> subscriptions = subscriptionManager.getSubscriptions(buildResult.getProject());
        for (Subscription subscription : subscriptions)
        {
            if (subscription.conditionSatisfied(buildResult))
            {
                subscription.getContactPoint().notify(buildResult);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCompletedEvent.class};
    }
}
