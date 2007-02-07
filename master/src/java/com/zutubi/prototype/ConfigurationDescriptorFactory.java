package com.zutubi.prototype;

import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.prototype.record.RecordTypeInfo;

/**
 *
 *
 */
public class ConfigurationDescriptorFactory
{
    private RecordTypeRegistry recordTypeRegistry;

    public ConfigurationDescriptor createDescriptor(String symbolicName)
    {
        RecordTypeInfo typeInfo = recordTypeRegistry.getInfo(symbolicName);

        ConfigurationDescriptor descriptor = new ConfigurationDescriptor();
        descriptor.setTypeInfo(typeInfo);
        return descriptor;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
