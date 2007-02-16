package com.zutubi.prototype;

import com.zutubi.prototype.annotation.AnnotationHandler;
import com.zutubi.prototype.annotation.Handler;
import com.zutubi.prototype.type.PrimitiveType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.ListType;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.pulse.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class FormDescriptorFactory
{
    private static final Logger LOG = Logger.getLogger(FormDescriptorFactory.class);

    private static final Map<Class, String> defaultFieldTypeMapping = new HashMap<Class, String>();
    static
    {
        defaultFieldTypeMapping.put(String.class, "text");
        defaultFieldTypeMapping.put(Boolean.class, "checkbox");
        defaultFieldTypeMapping.put(Boolean.TYPE, "checkbox");
    }

    private TypeRegistry typeRegistry;

    public FormDescriptor createDescriptor(Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        return createDescriptor(type);
    }

    public FormDescriptor createDescriptor(String symbolicName)
    {
        CompositeType type = typeRegistry.getType(symbolicName);
        return createDescriptor(type);
    }

    public FormDescriptor createDescriptor(CompositeType type)
    {
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setType(type);

        List<Annotation> annotations = type.getAnnotations();
        handleAnnotations(descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(type));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(CompositeType type)
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (String propertyName : type.getProperties(PrimitiveType.class))
        {
            Type propertyType = type.getProperty(propertyName);
            FieldDescriptor fd = new FieldDescriptor();
            fd.setName(propertyName);

            // some little bit of magic, take a guess at any property called password. If we come up with any
            // other magical cases, then we can refactor this a bit.
            if (fd.getName().equals("password"))
            {
                fd.addParameter("type", "password");
            }
            else
            {
                fd.addParameter("type", defaultFieldTypeMapping.get(propertyType.getClazz()));
            }

            handleAnnotations(fd, type.getProperty(propertyName).getAnnotations());

            fieldDescriptors.add(fd);
        }

        for (FieldDescriptor fd : fieldDescriptors)
        {
            String propertyName = fd.getName();
            if (type.hasProperty(propertyName + "Options"))
            {
                Type optionsProperty = type.getProperty(propertyName + "Options");
                if (optionsProperty instanceof ListType)
                {
                    Type propertyType = type.getProperty(propertyName);
                    // ensure that the option type is the same as the field type.
                    ListType listType = (ListType) optionsProperty;
                    if (listType.getCollectionType().getClazz() == propertyType.getClazz())
                    {
                        fd.addParameter("type", "select");
                    }
                }
            }
        }

        return fieldDescriptors;
    }

    private void handleAnnotations(Descriptor descriptor, List<Annotation> annotations)
    {
        // need to recurse over annotations, ignoring the java.lang annotations.
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().getName().startsWith("java.lang"))
            {
                // ignore standard ann10otations.
                continue;
            }

            // recurse up the annotation hierarchy.
            handleAnnotations(descriptor, Arrays.asList(annotation.annotationType().getAnnotations()));

            if (annotation.annotationType().isAnnotationPresent(Handler.class))
            {
                Handler handlerAnnotation = annotation.annotationType().getAnnotation(Handler.class);
                try
                {
                    AnnotationHandler handler = handlerAnnotation.value().newInstance();
                    handler.process(annotation, descriptor);
                }
                catch (InstantiationException e)
                {
                    LOG.warning(e); // failed to instantiate the annotation handler.
                }
                catch (IllegalAccessException e)
                {
                    LOG.warning(e); // failed to instantiate the annotation handler.
                }
            }
        }
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance.
     */
    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
