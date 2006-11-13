package com.zutubi.pulse.form.persist.reflection;

import com.zutubi.pulse.form.persist.DescriptorFactory;
import com.zutubi.pulse.form.persist.PersistenceDescriptor;
import com.zutubi.pulse.form.persist.DefaultPersistenceDescriptor;

import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * <class-comment/>
 */
public class ReflectionDescriptorFactory implements DescriptorFactory
{
    public PersistenceDescriptor createDescriptor(Class type)
    {
        // look for a field called id with a serializable type.
        PersistenceDescriptor descriptor = new DefaultPersistenceDescriptor();

        try
        {
            BeanInfo bean = Introspector.getBeanInfo(type);
            for (PropertyDescriptor property : bean.getPropertyDescriptors())
            {
                if ("id".equals(property.getName()))
                {
                    // requirement: read and write methods are defined.
                    descriptor.setIdProperty(property.getName());
                    descriptor.setReaderMethod(property.getReadMethod());
                    descriptor.setWriterMethod(property.getWriteMethod());
                    break;
                }
            }
        }
        catch (IntrospectionException e)
        {
            e.printStackTrace();
        }

        return descriptor;
    }
}
