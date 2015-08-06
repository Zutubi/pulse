package com.zutubi.pulse.master.rest.model;

import java.util.List;

/**
 * Toy model class while working on RESTish API.
 */
public abstract class ConfigModel
{
    private final String kind;
    private final String key;
    private final String label;
    private TypeModel type;
    private String iconClass;
    private List<ConfigModel> children;

    protected ConfigModel(String kind, String key, String label)
    {
        this.kind = kind;
        this.key = key;
        this.label = label;
    }

    public String getKind()
    {
        return kind;
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

    public List<ConfigModel> getChildren()
    {
        return children;
    }

    public void setChildren(List<ConfigModel> children)
    {
        this.children = children;
    }
}
