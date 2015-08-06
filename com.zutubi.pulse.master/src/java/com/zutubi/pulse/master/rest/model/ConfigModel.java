package com.zutubi.pulse.master.rest.model;

import java.util.List;

/**
 * Toy model class while working on RESTish API.
 */
public abstract class ConfigModel
{
    private final String kind;
    private final String handle;
    private final String key;
    private final String label;
    private TypeModel type;
    private String iconClass;
    private List<ConfigModel> nested;

    protected ConfigModel(String kind, String handle, String key, String label)
    {
        this.kind = kind;
        this.handle = handle;
        this.key = key;
        this.label = label;
    }

    public String getKind()
    {
        return kind;
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

    public TypeModel getType()
    {
        return type;
    }

    public void setType(TypeModel type)
    {
        this.type = type;
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
