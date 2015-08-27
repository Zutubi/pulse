package com.zutubi.pulse.master.rest.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model representing composites.
 */
@JsonTypeName("composite")
public class CompositeModel extends ConfigModel
{
    private CompositeTypeModel type;
    private Map<String, Object> properties;
    private Map<String, Object> formattedProperties;
    private List<ActionModel> actions;

    public CompositeModel()
    {
    }

    public CompositeModel(String handle, String key, String label)
    {
        super(handle, key, label);
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public void setType(CompositeTypeModel type)
    {
        this.type = type;
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, Object> properties)
    {
        this.properties = properties;
    }

    public Map<String, Object> getFormattedProperties()
    {
        return formattedProperties;
    }

    public void setFormattedProperties(Map<String, Object> formattedProperties)
    {
        this.formattedProperties = formattedProperties;
    }

    public List<ActionModel> getActions()
    {
        return actions;
    }

    public void addAction(ActionModel action)
    {
        if (actions == null)
        {
            actions = new ArrayList<>();
        }
        actions.add(action);
    }

}
