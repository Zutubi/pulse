package com.zutubi.pulse.form.ui;

import java.util.Map;
import java.io.Writer;

/**
 * <class-comment/>
 */
public class TemplateRendererContext
{
    private String name;

    private Map<String, Object> parameters;

    private RenderContext context;

    public TemplateRendererContext(String name, Map<String, Object> parameters, RenderContext context)
    {
        this.name = name;
        this.parameters = parameters;
        this.context = context;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public Object get(String key)
    {
        return context.get(key);
    }

    public Writer getWriter()
    {
        return context.getWriter();
    }
}
