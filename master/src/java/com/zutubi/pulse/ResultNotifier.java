package com.zutubi.pulse;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.condition.UnsuccessfulCountBuildsValue;
import com.zutubi.pulse.condition.UnsuccessfulCountDaysValue;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.user.SubscriptionConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.renderer.BuildResultRenderer;
import com.zutubi.pulse.webwork.mapping.Urls;
import com.zutubi.util.logging.Logger;

import java.io.StringWriter;
import java.util.*;

/**
 *
 */
public class ResultNotifier implements EventListener
{
    public static final String FAILURE_LIMIT_PROPERTY = "pulse.notification.test.failure.limit";
    public static final int DEFAULT_FAILURE_LIMIT = 20;

    private static final Logger LOG = Logger.getLogger(ResultNotifier.class);

    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;
    private BuildResultRenderer buildResultRenderer;
    private BuildManager buildManager;

    public static int getFailureLimit()
    {
        int limit = DEFAULT_FAILURE_LIMIT;
        String property = System.getProperty(FAILURE_LIMIT_PROPERTY);
        if(property != null)
        {
            try
            {
                limit = Integer.parseInt(property);
            }
            catch(NumberFormatException e)
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
        Map<String, Object> dataMap = getDataMap(buildResult, configurationProvider.get(GlobalConfiguration.class).getBaseUrl(), buildManager, buildResultRenderer);

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
                String templateName = subscription.getTemplate();
                RenderedResult rendered = renderResult(buildResult, dataMap, buildResultRenderer, templateName, renderCache);
                notifiedContactPoints.add(contactPoint.getHandle());
                contactPoint.notify(buildResult, rendered.subject, rendered.content, buildResultRenderer.getTemplateInfo(templateName, buildResult.isPersonal()).getMimeType());
                
                // Contact point may be modified: e.g. error may be set.
                configurationProvider.save(contactPoint);
            }
        }
    }

    public static Map<String, Object> getDataMap(BuildResult result, String baseUrl, BuildManager buildManager, BuildResultRenderer renderer)
    {
        Project project = result.getProject();

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("renderer", renderer);
        dataMap.put("baseUrl", baseUrl);
        dataMap.put("externalUrls", new Urls(baseUrl));
        dataMap.put("project", project);
        dataMap.put("status", result.succeeded() ? "healthy" : "broken");
        dataMap.put("result", result);
        dataMap.put("model", result);
        dataMap.put("changelists", buildManager.getChangesForBuild(result));
        dataMap.put("errorLevel", Feature.Level.ERROR);
        dataMap.put("warningLevel", Feature.Level.WARNING);

        if(!result.succeeded())
        {
            BuildResult lastSuccess = buildManager.getLatestSuccessfulBuildResult();
            if (lastSuccess != null)
            {
                dataMap.put("lastSuccess", lastSuccess);
            }
            
            dataMap.put("unsuccessfulBuilds", UnsuccessfulCountBuildsValue.getValueForBuild(result, buildManager));
            dataMap.put("unsuccessfulDays", UnsuccessfulCountDaysValue.getValueForBuild(result, buildManager));
        }

        return dataMap;
    }

    public static RenderedResult renderResult(BuildResult buildResult, String baseUrl, BuildManager buildManager, BuildResultRenderer buildResultRenderer, String template)
    {
        Map<String, Object> dataMap = getDataMap(buildResult, baseUrl, buildManager, buildResultRenderer);
        return renderResult(buildResult, dataMap, buildResultRenderer, template, null);
    }

    private static RenderedResult renderResult(BuildResult result, Map<String, Object> dataMap, BuildResultRenderer buildResultRenderer, String template, Map<String, RenderedResult> cache)
    {
        RenderedResult rendered = cache == null ? null : cache.get(template);
        if(rendered == null)
        {
            StringWriter w = new StringWriter();
            buildResultRenderer.render(result, dataMap, template, w);
            String content = w.toString();

            String subject;
            String subjectTemplate = template + "-subject";
            if(buildResultRenderer.hasTemplate(subjectTemplate, result.isPersonal()))
            {
                w = new StringWriter();
                buildResultRenderer.render(result, dataMap, subjectTemplate, w);
                subject = w.toString().trim();
            }
            else
            {
                subject = getDefaultSubject(result);
            }

            rendered = new RenderedResult(subject, content);
            if (cache != null)
            {
                cache.put(template, rendered);
            }
        }

        return rendered;
    }

    private static String getDefaultSubject(BuildResult result)
    {
        ProjectConfiguration config = result.getProject().getConfig();
        String prelude = result.isPersonal() ? "personal build " : (config.getName() + ": build ");
        return prelude + Long.toString(result.getNumber()) + ": " + result.getState().getPrettyString();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildCompletedEvent.class};
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
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

    public static class RenderedResult
    {
        String subject;
        String content;

        public RenderedResult(String subject, String content)
        {
            this.subject = subject;
            this.content = content;
        }

        public String getSubject()
        {
            return subject;
        }

        public String getContent()
        {
            return content;
        }
    }
}
