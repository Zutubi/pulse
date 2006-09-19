package com.zutubi.pulse.form.ui.renderers;

import com.zutubi.pulse.form.ui.Renderable;
import com.zutubi.pulse.form.ui.Renderer;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class FreemarkerRenderer implements Renderer
{
    public static final String DEFAULT_THEME = "xhtml";

    private Configuration configuration;

    private StringWriter writer = new StringWriter();

    private String theme = DEFAULT_THEME;

    private Map<String, Object> additionalContext;

    private boolean error;

    public void setAdditionalContext(Map<String, Object> context)
    {
        this.additionalContext = context;
    }

    public void render(Renderable r)
    {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("model", r);

        if (additionalContext != null)
        {
            context.putAll(additionalContext);
        }

        if (r.getContext() != null)
        {
            context.putAll(r.getContext());
        }

        String templateName = getTemplateName(r);
        doRender(r, context, templateName);
    }

    protected String getTemplateName(Renderable r)
    {
        return "/forms/" + theme + "/" + r.getTemplateName() + ".ftl";
    }

    protected void doRender(Renderable r, Map<String, Object> context, String templateName)
    {
        try
        {
            Template template = configuration.getTemplate(templateName);
            template.process(context, writer);
            writer.flush();
        }
        catch (Exception e)
        {
            error = true;
            e.printStackTrace();
            writer.append(e.getMessage());
        }
    }

    public boolean hasError()
    {
        return this.error;
    }

    public String getRenderedContent()
    {
        return writer.toString();
    }

    public void reset()
    {
        writer = new StringWriter();
        error = false;
    }

    /**
     * Required resource.
     *
     * @param configuration
     */
    public void setFreemarkerConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }
}
