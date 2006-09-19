package com.zutubi.pulse.form.ui.components;

import com.zutubi.pulse.form.ui.Renderable;
import com.zutubi.pulse.form.ui.Renderer;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public abstract class Component implements Renderable
{
    protected Map<String, Object> parameters = new HashMap<String, Object>();

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void addParameter(String name, Object value)
    {
        this.parameters.put(name, value);
    }

    public void addParameters(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public Object getParameter(String name)
    {
        return this.parameters.get(name);
    }

    public Object getParameter(String name, Object defaultValue)
    {
        if (this.parameters.containsKey(name))
        {
            return this.parameters.get(name);
        }
        return defaultValue;
    }

    public void removeParameter(String s)
    {
        this.parameters.remove(s);
    }

    public void render(Renderer r)
    {
        r.render(this);
    }

    public Map<String, Object> getContext()
    {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("parameters", getParameters());
        return context;
    }

    public void setName(String name)
    {
        addParameter("name", name);
    }

    public String getName()
    {
        return (String) getParameter("name");
    }

    public void setValue(Object value)
    {
        addParameter("value", value);
    }

    public Object getValue()
    {
        return getParameter("value");
    }

    public void setLabel(String label)
    {
        addParameter("label", label);
    }

    public String getLabel()
    {
        return (String) getParameter("label");
    }

    public void setTabIndex(int i)
    {
        addParameter("tabindex", Integer.toString(i));
    }

    public void setId(String id)
    {
        addParameter("id", id);
    }

    public void setClass(String clazz)
    {
        addParameter("cssClass", clazz);
    }

    public void setStyle(String style)
    {
        addParameter("cssStyle", style);
    }
}
