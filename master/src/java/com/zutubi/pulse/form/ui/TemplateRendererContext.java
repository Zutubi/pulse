package com.zutubi.pulse.form.ui;

import java.util.Map;

/**
 * <class-comment/>
 */
public class TemplateRendererContext
{
    private String name;

    private Map<String, Object> parameters;

    public TemplateRendererContext(String name, Map<String, Object> parameters)
    {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
