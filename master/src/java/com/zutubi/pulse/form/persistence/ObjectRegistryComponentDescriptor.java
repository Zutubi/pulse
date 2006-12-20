package com.zutubi.pulse.form.persistence;

import com.zutubi.plugins.internal.ComponentDescriptorSupport;
import nu.xom.Element;

/**
 * <class-comment/>
 */
public class ObjectRegistryComponentDescriptor extends ComponentDescriptorSupport
{
    private Class objectClass;

    private ObjectRegistry objectRegistry;

    public void init(Element config)
    {
        super.init(config);

        try
        {
            String className = config.getAttributeValue("class");

            Class clazz = plugin.loadClass(className, ObjectRegistryComponentDescriptor.class);
            if (Copyable.class.isAssignableFrom(clazz))
            {
                objectClass = clazz;
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }


    public void enable()
    {
        super.enable();

        objectRegistry.register(getKey(), objectClass);
    }

    public void disable()
    {
        super.disable();
    }

    public void setObjectRegistry(ObjectRegistry objectRegistry)
    {
        this.objectRegistry = objectRegistry;
    }
}
