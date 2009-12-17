package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.notifications.condition.UnsuccessfulCountBuildsValue;
import com.zutubi.pulse.master.notifications.condition.UnsuccessfulCountDaysValue;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class DefaultRenderService implements RenderService
{
    public Map<String, Object> getDataMap(BuildResult result, String baseUrl, BuildManager buildManager, BuildResultRenderer renderer)
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
        dataMap.put("infoLevel", Feature.Level.INFO);
        dataMap.put("warningLevel", Feature.Level.WARNING);

        if (!result.succeeded())
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

    public RenderedResult renderResult(BuildResult buildResult, String baseUrl, BuildManager buildManager, BuildResultRenderer buildResultRenderer, String template)
    {
        Map<String, Object> dataMap = getDataMap(buildResult, baseUrl, buildManager, buildResultRenderer);
        return renderResult(buildResult, dataMap, buildResultRenderer, template, null);
    }

    public RenderedResult renderResult(BuildResult result, Map<String, Object> dataMap, BuildResultRenderer buildResultRenderer, String template, Map<String, RenderedResult> cache)
    {
        RenderedResult rendered = cache == null ? null : cache.get(template);
        if (rendered == null)
        {
            StringWriter w = new StringWriter();
            buildResultRenderer.render(result, dataMap, template, w);
            String content = w.toString();

            String subject;
            String subjectTemplate = template + "-subject";
            if (buildResultRenderer.hasTemplate(subjectTemplate, result.isPersonal()))
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

    public String getDefaultSubject(BuildResult result)
    {
        ProjectConfiguration config = result.getProject().getConfig();
        String prelude = result.isPersonal() ? "personal build " : (config.getName() + ": build ");
        return prelude + Long.toString(result.getNumber()) + ": " + result.getState().getPrettyString();
    }
}
