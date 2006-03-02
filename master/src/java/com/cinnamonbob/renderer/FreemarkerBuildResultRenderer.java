package com.cinnamonbob.renderer;

import com.cinnamonbob.core.model.Feature;
import com.cinnamonbob.core.util.StringUtils;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class FreemarkerBuildResultRenderer implements BuildResultRenderer
{
    private static final Logger LOG = Logger.getLogger(FreemarkerBuildResultRenderer.class);

    private Configuration freemarkerConfiguration;

    public void render(String hostUrl, BuildResult result, String type, Writer writer)
    {
        Map<String, Object> dataMap = new HashMap<String, Object>();

        dataMap.put("renderer", this);
        dataMap.put("type", type);
        dataMap.put("hostname", hostUrl);
        dataMap.put("result", result);
        dataMap.put("model", result);
        dataMap.put("errorLevel", Feature.Level.ERROR);
        dataMap.put("warningLevel", Feature.Level.WARNING);

        try
        {
            Template template = freemarkerConfiguration.getTemplate(type + File.separatorChar + "BuildResult.ftl");
            template.process(dataMap, writer);
            writer.flush();
        }
        catch (Exception e)
        {
            // TemplateExceptions also end up in the writer output
            LOG.warning("Unable to render build result: " + e.getMessage(), e);
        }
    }

    public String trimmedString(String s, int length)
    {
        return StringUtils.trimmedString(s, length);
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration)
    {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }
}
