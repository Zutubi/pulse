package com.zutubi.pulse.master.notifications.renderer;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.notifications.NotificationAttachment;
import com.zutubi.pulse.master.notifications.condition.BrokenCountBuildsValue;
import com.zutubi.pulse.master.notifications.condition.BrokenCountDaysValue;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
        dataMap.put("status", result.healthy() ? "healthy" : "broken");
        dataMap.put("result", result);
        dataMap.put("model", result);
        dataMap.put("changelists", buildManager.getChangesForBuild(result, 0, true));
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

            dataMap.put("unsuccessfulBuilds", BrokenCountBuildsValue.getValueForBuild(result, buildManager));
            dataMap.put("unsuccessfulDays", BrokenCountDaysValue.getValueForBuild(result, buildManager));
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
            String mimeType = buildResultRenderer.getTemplateInfo(template, result.isPersonal()).getMimeType();

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

            rendered = new RenderedResult(subject, content, mimeType);
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

    public List<NotificationAttachment> getAttachments(BuildResult buildResult, boolean attachLogs, int logLineLimit, boolean includeBuildLog, MasterConfigurationManager configurationManager)
    {
        List<NotificationAttachment> attachments = new LinkedList<NotificationAttachment>();
        if (attachLogs)
        {
            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            if (includeBuildLog)
            {
                BuildLogFile buildLog = new BuildLogFile(buildResult, paths);
                attachments.add(new NotificationAttachment("build.log", buildLog, logLineLimit));
            }

            for (RecipeResultNode node: buildResult.getStages())
            {
                attachments.add(new NotificationAttachment("stage-" + node.getStageName() + ".log", new RecipeLogFile(buildResult,  node.getResult().getId(), paths), logLineLimit));
            }
        }

        return attachments;
    }
}
