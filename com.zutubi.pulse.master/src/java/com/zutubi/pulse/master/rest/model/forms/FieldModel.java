package com.zutubi.pulse.master.rest.model.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single field in a form.
 */
public class FieldModel
{
    private String type;
    private String name;
    private String label;
    private boolean required;
    private boolean readOnly;

    private List<String> actions;
    private List<String> scripts;

    private Map<String, Object> parameters = new HashMap<>();

    public FieldModel()
    {
    }

    public FieldModel(String type, String name, String label)
    {
        this.type = type;
        this.name = name;
        this.label = label;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public List<String> getActions()
    {
        return actions;
    }

    public void addAction(String action)
    {
        if (actions == null)
        {
            actions = new ArrayList<>();
        }

        actions.add(action);
    }

    public List<String> getScripts()
    {
        return scripts;
    }

    public void addScript(String script)
    {
        if (scripts == null)
        {
            scripts = new ArrayList<>();
        }

        scripts.add(script);
    }

    public boolean hasParameter(String name)
    {
        return parameters.containsKey(name);
    }

    public Object getParameter(String name)
    {
        return parameters.get(name);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void addParameter(String name, Object value)
    {
        parameters.put(name, value);
    }
}
