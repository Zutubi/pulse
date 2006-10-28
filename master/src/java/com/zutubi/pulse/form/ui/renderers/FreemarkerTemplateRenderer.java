package com.zutubi.pulse.form.ui.renderers;

import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FreemarkerTemplateRenderer implements TemplateRenderer
{
    private Configuration configuration;

    private Writer writer;

    private String defaultTemplateDir = "forms";
    private String defaultTheme = "custom";

    public void render(TemplateRendererContext rendererContext) throws Exception
    {
        // build the template name based on the context details.
        // templateDir/theme/templateName.

        String templateName = "/"+defaultTemplateDir+"/"+defaultTheme+"/" + rendererContext.getName() + ".ftl";

        Map<String, Object> context = new HashMap<String, Object>();
        Map<String, Object> params = rendererContext.getParameters();
        if (!params.containsKey("templateDir"))
        {
            params.put("templateDir", defaultTemplateDir);
        }
        if (!params.containsKey("theme"))
        {
            params.put("theme", defaultTheme);
        }

        context.put("parameters", rendererContext.getParameters());
        context.put("fieldErrors", rendererContext.get("fieldErrors"));

        Template template = configuration.getTemplate(templateName);
        template.process(context, writer);
        writer.flush();
    }

    public void setWriter(Writer writer)
    {
        this.writer = writer;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
