package com.zutubi.tove.config;

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.Type;

/**
 * Holds information about a root configuration scope.
 */
public class ConfigurationScopeInfo
{
    private String scopeName;
    private ComplexType type;
    private boolean persistent;

    public ConfigurationScopeInfo(String scopeName, ComplexType type, boolean persistent)
    {
        this.scopeName = scopeName;
        this.type = type;
        this.persistent = persistent;
    }

    public String getScopeName()
    {
        return scopeName;
    }

    public ComplexType getType()
    {
        return type;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public Type getTargetType()
    {
        return type.getTargetType();
    }

    public boolean isCollection()
    {
        return type instanceof CollectionType;
    }

    public boolean isTemplated()
    {
        return type.isTemplated();
    }
}
