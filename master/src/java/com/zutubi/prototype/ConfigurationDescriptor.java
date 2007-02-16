package com.zutubi.prototype;

import com.zutubi.prototype.model.Config;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.PrimitiveType;

import java.util.List;

/**
 *
 *
 */
public class ConfigurationDescriptor
{
    private CompositeType type;

    public void setType(CompositeType type)
    {
        this.type = type;
    }

    public Config instantiate(Object value)
    {
        Config config = new Config();
        List<String> simpleInfos = type.getProperties(PrimitiveType.class);
        for (String name : simpleInfos)
        {
            config.addSimpleProperty(name);
        }

        List<String> valueListInfos = type.getProperties(ListType.class);
        for (String name : valueListInfos)
        {
            config.addValueListProperty(name);
        }

        List<String> subrecordInfos = type.getProperties(CompositeType.class);
        for (String name: subrecordInfos)
        {
            config.addNestedProperty(name);
        }

        List<String> extensionInfos = type.getExtensions();
        for (String name : extensionInfos)
        {
            config.addExtension(name);
        }

        return config;
    }

}
