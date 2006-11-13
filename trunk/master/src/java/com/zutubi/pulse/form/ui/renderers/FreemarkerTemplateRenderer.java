package com.zutubi.pulse.form.ui.renderers;

import com.zutubi.pulse.form.ui.TemplateRenderer;
import com.zutubi.pulse.form.ui.TemplateRendererContext;
import com.zutubi.pulse.util.logging.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.Writer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class FreemarkerTemplateRenderer implements TemplateRenderer
{
    private static final Logger LOG = Logger.getLogger(FreemarkerTemplateRenderer.class);

    private Configuration configuration;

    //TODO: these properties should be located externally.
    private static final String DEFAULT_TEMPLATE_DIR = "forms";
    private static final String DEFAULT_THEME = "custom";

    private String templateDir = DEFAULT_TEMPLATE_DIR;
    
    private String theme = DEFAULT_THEME;

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public void render(TemplateRendererContext rendererContext) throws Exception
    {
        // build the template name based on the context details.
        // templateDir/theme/templateName.

        String templateName = "/"+ templateDir +"/"+ theme +"/" + rendererContext.getName() + ".ftl";

        Map<String, Object> context = new HashMap<String, Object>();
        Map<String, Object> params = rendererContext.getParameters();
        if (!params.containsKey("templateDir"))
        {
            params.put("templateDir", templateDir);
        }
        if (!params.containsKey("theme"))
        {
            params.put("theme", theme);
        }

        context.put("parameters", rendererContext.getParameters());
        context.put("fieldErrors", rendererContext.get("fieldErrors"));

        try
        {
            Template template = configuration.getTemplate(templateName);
            template.process(context, rendererContext.getWriter());
        }
        catch (FileNotFoundException e)
        {
            // This means the template was not found. Do we want to allow this?
            LOG.warning(e.getMessage());
        }

        // Should we flush or is it enough to rely upon the creating class to handle the
        // writer flushing duties?  Disabling for now.
        //rendererContext.getWriter().flush();
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
