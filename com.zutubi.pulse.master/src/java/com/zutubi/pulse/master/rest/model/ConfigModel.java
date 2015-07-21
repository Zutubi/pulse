package com.zutubi.pulse.master.rest.model;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.Type;

import java.util.List;

/**
 * Toy model class while working on RESTish API.
 */
public class ConfigModel
{
    private List<String> simpleProperties;

    public ConfigModel(Configuration config, Type type)
    {
        if (type instanceof CompositeType)
        {
            CompositeType compositeType = (CompositeType) type;
            simpleProperties = compositeType.getSimplePropertyNames();
        }
    }

    public List<String> getSimpleProperties()
    {
        return simpleProperties;
    }
}
