package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.descriptor.DescriptorDecorator;
import com.zutubi.pulse.form.descriptor.DescriptorFactory;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.FieldTypeRegistry;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class ReflectionDescriptorFactory implements DescriptorFactory
{
    private final Map<Class, FormDescriptor> formDescriptorCache = new HashMap<Class, FormDescriptor>();

    private FieldTypeRegistry fieldTypeRegistry;

    private List<DescriptorDecorator> decorators = new LinkedList<DescriptorDecorator>();

    public FormDescriptor createFormDescriptor(Class type)
    {
        if (formDescriptorCache.containsKey(type))
        {
            return formDescriptorCache.get(type);
        }

        ReflectionFormDescriptor formDescriptor = new ReflectionFormDescriptor();
        formDescriptor.setType(type);
        formDescriptor.setFieldDescriptors(buildFieldDescriptors(type));

        FormDescriptor decoratedDescriptor = applyDecorators(formDescriptor);
        formDescriptorCache.put(type, decoratedDescriptor);

        return decoratedDescriptor;
    }

    private FormDescriptor applyDecorators(FormDescriptor formDescriptor)
    {
        for (DescriptorDecorator decorator : decorators)
        {
            formDescriptor = decorator.decorate(formDescriptor);
        }
        return formDescriptor;
    }

    public void setDecorators(List<DescriptorDecorator> decorators)
    {
        this.decorators = decorators;
    }

    public void addDecorator(DescriptorDecorator decorator)
    {
        this.decorators.add(decorator);
    }

    private List<FieldDescriptor> buildFieldDescriptors(Class type)
    {
        try
        {
            List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

            BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);

            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
            {
                ReflectionFieldDescriptor fd = new ReflectionFieldDescriptor();
                fd.setName(pd.getName());
                fd.setType(pd.getPropertyType());
                fd.setFieldType(fieldTypeRegistry.getDefaultFieldType(pd.getPropertyType()));

                fieldDescriptors.add(fd);
            }

            return fieldDescriptors;
        }
        catch (IntrospectionException e)
        {
            throw new ReflectionException(e);
        }
    }

    public void setFieldTypeRegistry(FieldTypeRegistry fieldTypeRegistry)
    {
        this.fieldTypeRegistry = fieldTypeRegistry;
    }
}
