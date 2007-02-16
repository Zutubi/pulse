package com.zutubi.prototype;

import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.CompositeType;

/**
 *
 *
 */
public class ConfigurationDescriptorFactory
{
    private TypeRegistry typeRegistry;

    public ConfigurationDescriptor createDescriptor(String symbolicName)
    {
        CompositeType typeInfo = typeRegistry.getType(symbolicName);

        ConfigurationDescriptor descriptor = new ConfigurationDescriptor();
        descriptor.setType(typeInfo);
        return descriptor;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
