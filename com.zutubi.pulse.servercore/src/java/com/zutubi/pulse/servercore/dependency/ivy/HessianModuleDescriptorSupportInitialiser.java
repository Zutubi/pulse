package com.zutubi.pulse.servercore.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.servercore.hessian.CustomSerialiserFactory;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

/**
 * This class initialises the ModuleDescriptor instance serialisation/deserialisation
 * support within Hessian.
 */
public class HessianModuleDescriptorSupportInitialiser
{
    private CustomSerialiserFactory customSerialiserFactory = null;

    public void init()
    {
        ModuleDescriptorDeserialiser deserialiser = new ModuleDescriptorDeserialiser();
        ModuleDescriptorSerialiser serialiser = new ModuleDescriptorSerialiser();

        customSerialiserFactory.register(ModuleDescriptor.class, serialiser, deserialiser);
    }

    public void setCustomSerialiserFactory(CustomSerialiserFactory customSerialiserFactory)
    {
        this.customSerialiserFactory = customSerialiserFactory;
    }
}
