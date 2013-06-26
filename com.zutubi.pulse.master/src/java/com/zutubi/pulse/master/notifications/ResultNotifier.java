package com.zutubi.pulse.master.notifications;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.notifications.renderer.RenderService;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.pulse.master.security.Principle;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.user.SubscriptionConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.transaction.TransactionManager;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * When a build completes, notifies subscribed contact points of the results.
 * Handles formatting of notification messages, ensuring that the result is
 * only rendered once per template required for all subscriptions.
 */
public class ResultNotifier implements EventListener
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.notification.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 20;

    private static final Logger LOG = Logger.getLogger(ResultNotifier.class);

    private Map<Long, String> contactPointErrors = new HashMap<Long, String>();
    private Lock contactPointErrorsLock = new ReentrantLock();

    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;
    private EventManager eventManager;
    private ThreadFactory threadFactory;
    private AccessManager accessManager;
    private UserManager userManager;
    private RenderService renderService;
    private TransactionManager transactionManager;
    private ObjectFactory objectFactory;
    private TransactionContext transactionContext;

    public void init()
    {
        AsynchronousDelegatingListener listener = new AsynchronousDelegatingListener(this, getClass().getSimpleName(), Executors.newCachedThreadPool(threadFactory));
        eventManager.register(listener);
    }

    public static int getFailureLimit()
    {
        int limit = DEFAULT_FAILURE_LIMIT;
        String property = System.getProperty(FAILURE_LIMIT_PROPERTY);
        if (property != null)
        {
            try
            {
                limit = Integer.parseInt(property);
            }
            catch (NumberFormatException e)
            {
                LOG.warning(e);
            }
        }

        return limit;
    }

    public void handleEvent(Event evt)
    {
        BuildCompletedEvent event = (BuildCompletedEvent) evt;
        final BuildResult buildResult = event.getBuildResult();

        buildResult.loadFailedTestResults(configurationManager.getDataDirectory(), getFailureLimit());

        // Evaluate all of the conditions first, in a single transaction so the session is shared.
        Set<SubscriptionConfiguration> subscriptionsToNotify = transactionContext.executeInsideTransaction(new NullaryFunction<Set<SubscriptionConfiguration>>()
        {
            public Set<SubscriptionConfiguration> process()
            {
                Set<SubscriptionConfiguration> subscriptionsToNotify = new HashSet<SubscriptionConfiguration>();
                Set<Long> contactPointsToNotify = new HashSet<Long>();
                NotifyConditionContext context = objectFactory.buildBean(NotifyConditionContext.class, buildResult);
                for (SubscriptionConfiguration subscription : configurationProvider.getAll(SubscriptionConfiguration.class))
                {
                    ContactConfiguration contactPoint = subscription.getContact();
                    if (!contactPointsToNotify.contains(contactPoint.getHandle()) && subscription.conditionSatisfied(context))
                    {
                        contactPointsToNotify.add(contactPoint.getHandle());
                        subscriptionsToNotify.add(subscription);
                    }
                }

                return subscriptionsToNotify;
            }
        });

        // Now render and send notifications.
        Map<String, RenderedResult> renderCache = new HashMap<String, RenderedResult>();
        Map<String, Object> dataMap = renderService.getDataMap(buildResult, configurationProvider.get(GlobalConfiguration.class).getBaseUrl());
        for (SubscriptionConfiguration subscription: subscriptionsToNotify)
        {
            UserConfiguration userConfig = configurationProvider.getAncestorOfType(subscription, UserConfiguration.class);
            if (canView(userConfig, buildResult))
            {
                String templateName = subscription.getTemplate();
                RenderedResult rendered = renderService.renderResult(buildResult, dataMap, templateName, renderCache);
                notifyContactPoint(subscription.getContact(), buildResult, rendered, subscription);
            }
        }
    }

    private boolean canView(UserConfiguration userConfig, BuildResult buildResult)
    {
        Principle user = userManager.getPrinciple(userConfig);
        return accessManager.hasPermission(user, AccessManager.ACTION_VIEW, buildResult);
    }

    private void notifyContactPoint(ContactConfiguration contactPoint, BuildResult buildResult, RenderedResult rendered, SubscriptionConfiguration subscription)
    {
        clearError(contactPoint);
        try
        {
            List<NotificationAttachment> attachments = null;
            if (contactPoint.supportsAttachments())
            {
                attachments = renderService.getAttachments(buildResult, subscription.isAttachLogs(), subscription.getLogLineLimit(), true);
            }
            contactPoint.notify(rendered, attachments);
        }
        catch (Exception e)
        {
            String message = e.getClass().getName();
            if (e.getMessage() != null)
            {
                message += ": " + e.getMessage();
            }

            setError(contactPoint, message);
        }
    }

    public boolean hasError(ContactConfiguration contactPoint)
    {
        return getError(contactPoint) != null;
    }

    public String getError(ContactConfiguration contactPoint)
    {
        contactPointErrorsLock.lock();
        try
        {
            return contactPointErrors.get(contactPoint.getHandle());
        }
        finally
        {
            contactPointErrorsLock.unlock();
        }
    }

    public void clearError(ContactConfiguration contactPoint)
    {
        contactPointErrorsLock.lock();
        try
        {
            contactPointErrors.remove(contactPoint.getHandle());
        }
        finally
        {
            contactPointErrorsLock.unlock();
        }
    }

    private void setError(ContactConfiguration contactPoint, String message)
    {
        contactPointErrorsLock.lock();
        try
        {
            contactPointErrors.put(contactPoint.getHandle(), message);
        }
        finally
        {
            contactPointErrorsLock.unlock();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCompletedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setRenderService(RenderService renderService)
    {
        this.renderService = renderService;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
