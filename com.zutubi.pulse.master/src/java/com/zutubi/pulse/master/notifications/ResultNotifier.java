package com.zutubi.pulse.master.notifications;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.notifications.renderer.BuildResultRenderer;
import com.zutubi.pulse.master.notifications.renderer.DefaultRenderService;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.user.SubscriptionConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.logging.Logger;

import java.util.*;
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
    private BuildResultRenderer buildResultRenderer;
    private EventManager eventManager;
    private BuildManager buildManager;
    private ThreadFactory threadFactory;
    private AccessManager accessManager;
    private UserManager userManager;
    private DefaultRenderService renderService = new DefaultRenderService();

    public void init()
    {
        AsynchronousDelegatingListener listener = new AsynchronousDelegatingListener(this, threadFactory);
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
        BuildResult buildResult = event.getBuildResult();

        buildResult.loadFailedTestResults(configurationManager.getDataDirectory(), getFailureLimit());

        Set<Long> notifiedContactPoints = new HashSet<Long>();
        Map<String, RenderedResult> renderCache = new HashMap<String, RenderedResult>();
        Map<String, Object> dataMap = renderService.getDataMap(buildResult, configurationProvider.get(GlobalConfiguration.class).getBaseUrl(), buildManager, buildResultRenderer);

        Collection<SubscriptionConfiguration> subscriptions = configurationProvider.getAll(SubscriptionConfiguration.class);
        for (SubscriptionConfiguration subscription : subscriptions)
        {
            // filter out contact points that we have already notified.
            ContactConfiguration contactPoint = subscription.getContact();
            if (notifiedContactPoints.contains(contactPoint.getHandle()))
            {
                continue;
            }

            // determine which of these subscriptions should be notified.
            if (subscription.conditionSatisfied(buildResult))
            {
                UserConfiguration userConfig = configurationProvider.getAncestorOfType(subscription, UserConfiguration.class);
                if (canView(userConfig, buildResult))
                {
                    String templateName = subscription.getTemplate();
                    RenderedResult rendered = renderService.renderResult(buildResult, dataMap, buildResultRenderer, templateName, renderCache);
                    notifiedContactPoints.add(contactPoint.getHandle());

                    notifyContactPoint(contactPoint, buildResult, rendered, buildResultRenderer.getTemplateInfo(templateName, buildResult.isPersonal()).getMimeType());
                }
            }
        }
    }

    private boolean canView(UserConfiguration userConfig, BuildResult buildResult)
    {
        AcegiUser user = userManager.getPrinciple(userConfig);
        return accessManager.hasPermission(user, AccessManager.ACTION_VIEW, buildResult);
    }

    private void notifyContactPoint(ContactConfiguration contactPoint, BuildResult buildResult, RenderedResult rendered, String mimeType)
    {
        clearError(contactPoint);
        try
        {
            contactPoint.notify(buildResult, rendered.getSubject(), rendered.getContent(), mimeType);
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

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
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
}
