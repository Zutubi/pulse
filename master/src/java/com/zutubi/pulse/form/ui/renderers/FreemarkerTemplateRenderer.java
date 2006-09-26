package com.zutubi.pulse.form.ui.renderers;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.util.Map;
import java.util.HashMap;
import java.io.Writer;

import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;

/**
 * <class-comment/>
 */
public class FreemarkerTemplateRenderer implements TemplateRenderer
{
    private Configuration configuration;

    private Writer writer;

    public void render(TemplateRendererContext rendererContext) throws Exception
    {
        // build the template name based on the context details.
        // templateDir/theme/templateName.

        String templateName = "/forms/xhtml/" + rendererContext.getName() + ".ftl";

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("parameters", rendererContext.getParameters());

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
