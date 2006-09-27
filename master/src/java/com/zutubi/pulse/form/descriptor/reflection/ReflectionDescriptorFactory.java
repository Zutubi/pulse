package com.zutubi.pulse.form.descriptor.reflection;

import com.zutubi.pulse.form.descriptor.*;
import com.zutubi.pulse.form.FieldType;

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
    private static final Map<Class, String> defaultFieldTypeMapping = new HashMap<Class, String>();
    static
    {
        defaultFieldTypeMapping.put(String.class, "text");
        defaultFieldTypeMapping.put(Boolean.class, "checkbox");
        defaultFieldTypeMapping.put(Boolean.TYPE, "checkbox");
    }

    private final Map<Class, FormDescriptor> formDescriptorCache = new HashMap<Class, FormDescriptor>();

    private List<DescriptorDecorator> decorators = new LinkedList<DescriptorDecorator>();

    public FormDescriptor createFormDescriptor(Class type)
    {
/*
        if (formDescriptorCache.containsKey(type))
        {
            return formDescriptorCache.get(type);
        }
*/

        DefaultFormDescriptor formDescriptor = new DefaultFormDescriptor();
        formDescriptor.setType(type);
        formDescriptor.setFieldDescriptors(buildFieldDescriptors(type));

        FormDescriptor decoratedDescriptor = applyDecorators(formDescriptor);
/*
        formDescriptorCache.put(type, decoratedDescriptor);
*/

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

            // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors())
            {
                DefaultFieldDescriptor fd = new DefaultFieldDescriptor();
                fd.setName(pd.getName());
                if (pd.getPropertyType() == Boolean.TYPE)
                {
                    fd.setType(Boolean.class);
                }
                else
                {
                    fd.setType(pd.getPropertyType());
                }

                // some little bit of magic, take a guess at any property called password. If we come up with any
                // other magical cases, then we can refactor this a bit.
                if (fd.getName().equals("password"))
                {
                    fd.setFieldType("password");
                }
                else
                {
                    fd.setFieldType(defaultFieldTypeMapping.get(pd.getPropertyType()));
                }
                fieldDescriptors.add(fd);
            }

            // Handle the second pass analysis. It is here where fields are considered in groups.
            // a) look for xxx and xxxOptions properties.
            Map<String, FieldDescriptor> optionsCandidates = new HashMap<String, FieldDescriptor>();
            Map<String, FieldDescriptor> descriptorMap = new HashMap<String, FieldDescriptor>();
            for (FieldDescriptor fd : fieldDescriptors)
            {
                String name = fd.getName();
                if (name.endsWith("Options"))
                {
                    String property = name.substring(0, name.length() - 7);
                    optionsCandidates.put(property, fd);
                }
                descriptorMap.put(name, fd);
            }

            for (String candidate : optionsCandidates.keySet())
            {
                // does this candidate property exist?
                if (descriptorMap.containsKey(candidate))
                {
                    FieldDescriptor optionsDescriptor = optionsCandidates.get(candidate);
                    fieldDescriptors.remove(optionsDescriptor);

                    // now we need to update the field.
                    FieldDescriptor optionDescriptor = descriptorMap.get(candidate);
                    optionDescriptor.setFieldType(FieldType.SELECT);
                }
            }

            return fieldDescriptors;
        }
        catch (IntrospectionException e)
        {
            throw new ReflectionException(e);
        }
    }
}
