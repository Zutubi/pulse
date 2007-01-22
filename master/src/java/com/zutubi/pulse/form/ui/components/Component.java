package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.RenderContext;
import com.zutubi.pulse.form.ui.TemplateRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class Component
{
    protected RenderContext context;

    protected Map<String, Object> parameters;

    Component parent;

    public void setContext(RenderContext context)
    {
        this.context = context;
    }

    public void render(RenderContext context, TemplateRenderer renderer) throws Exception
    {

    }

/*
    public boolean start(ComponentRenderer renderer) throws Exception
    {
        return true;
    }

    public void body(ComponentRenderer renderer) throws Exception
    {
        
    }

    public boolean end(ComponentRenderer renderer) throws Exception
    {
        return false;
    }

*/
    protected Map<String, Object> getParameters()
    {
        if (parameters == null)
        {
            parameters = new HashMap<String, Object>();
        }
        return parameters;
    }

    public void addParameter(String key, Object value)
    {
        getParameters().put(key, value);
    }

    public void addParameters(Map<String, Object> params)
    {
        getParameters().putAll(params);
    }

    protected Component findAncestor(Class clazz)
    {
        if (parent == null)
        {
            return null;
        }
        if (parent.getClass() == clazz)
        {
            return parent;
        }
        return parent.findAncestor(clazz);
    }

    public String getId()
    {
        return (String) getParameters().get("id");
    }

    public void setId(String id)
    {
        addParameter("id", id);
    }

    protected String getText(String key)
    {
        String str = context.getText(key);
        if (str != null)
        {
            return str;
        }
        return key;
    }
}
