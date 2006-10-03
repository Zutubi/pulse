package com.zutubi.pulse.form.ui.renderers;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.util.Map;
import java.util.HashMap;
import java.io.Writer;

import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;
import com.zutubi.validation.ValidationContext;

/**
 * <class-comment/>
 */
public class FreemarkerTemplateRenderer implements TemplateRenderer
{
    private Configuration configuration;

    private ValidationContext validationContext;

    private Writer writer;

    private String defaultTemplateDir = "forms";
    private String defaultTheme = "custom";

    public void render(TemplateRendererContext rendererContext) throws Exception
    {
        // build the template name based on the context details.
        // templateDir/theme/templateName.

        String templateName = "/"+defaultTemplateDir+"/"+defaultTheme+"/" + rendererContext.getName() + ".ftl";

        Map<String, Object> context = new HashMap<String, Object>();
        Map params = rendererContext.getParameters();
        if (!params.containsKey("templateDir"))
        {
            params.put("templateDir", defaultTemplateDir);
        }
        if (!params.containsKey("theme"))
        {
            params.put("theme", defaultTheme);
        }

        context.put("parameters", rendererContext.getParameters());

        if (this.validationContext != null)
        {
            context.put("fieldErrors", validationContext.getFieldErrors());
        }

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

    public void setValidationContext(ValidationContext validationContext)
    {
        this.validationContext = validationContext;
    }
}
