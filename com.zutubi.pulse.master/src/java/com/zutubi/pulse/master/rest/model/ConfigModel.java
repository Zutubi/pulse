package com.zutubi.pulse.master.rest.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Base configuration model class for the RESTish API.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({
        @JsonSubTypes.Type(CollectionModel.class),
        @JsonSubTypes.Type(CompositeModel.class),
        @JsonSubTypes.Type(TypeSelectionModel.class)
})
public abstract class ConfigModel
{
    private final String handle;
    private final String key;
    private final String label;
    private String iconClass;
    private List<ConfigModel> nested;

    protected ConfigModel()
    {
        this(null, null, null);
    }

    protected ConfigModel(String handle, String key, String label)
    {
        this.handle = handle;
        this.key = key;
        this.label = label;
    }

    public String getHandle()
    {
        return handle;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public String getIconClass()
    {
        return iconClass;
    }

    public void setIconClass(String iconClass)
    {
        this.iconClass = iconClass;
    }

    public List<ConfigModel> getNested()
    {
        return nested;
    }

    public void setNested(List<ConfigModel> nested)
    {
        this.nested = nested;
    }
}
