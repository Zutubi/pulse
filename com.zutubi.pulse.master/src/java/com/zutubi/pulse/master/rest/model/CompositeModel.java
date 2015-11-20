package com.zutubi.pulse.master.rest.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.zutubi.tove.links.ConfigurationLink;

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
    private boolean keyed;
    private boolean concrete;
    private Map<String, Object> properties;
    private Map<String, Object> formattedProperties;
    private Map<String, List<String>> validationErrors;
    private List<ActionModel> actions;
    private List<ActionModel> descendantActions;
    private List<ConfigurationLink> links;

    public CompositeModel()
    {
    }

    public CompositeModel(String handle, String key, String label, boolean keyed, boolean concrete)
    {
        super(handle, key, label);
        this.keyed = keyed;
        this.concrete = concrete;
    }

    public CompositeTypeModel getType()
    {
        return type;
    }

    public void setType(CompositeTypeModel type)
    {
        this.type = type;
    }

    public boolean isKeyed()
    {
        return keyed;
    }

    public boolean isConcrete()
    {
        return concrete;
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

    public Map<String, List<String>> getValidationErrors()
    {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<String>> validationErrors)
    {
        this.validationErrors = validationErrors;
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

    public List<ActionModel> getDescendantActions()
    {
        return descendantActions;
    }

    public void addDescendantAction(ActionModel action)
    {
        if (descendantActions == null)
        {
            descendantActions = new ArrayList<>();
        }
        descendantActions.add(action);
    }

    public List<ConfigurationLink> getLinks()
    {
        return links;
    }

    public void setLinks(List<ConfigurationLink> links)
    {
        if (links.size() > 0)
        {
            this.links = new ArrayList<>(links);
        }
        else
        {
            this.links = null;
        }
    }
}
