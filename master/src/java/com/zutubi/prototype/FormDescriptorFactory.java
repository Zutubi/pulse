package com.zutubi.prototype;

import com.zutubi.prototype.annotation.AnnotationHandler;
import com.zutubi.prototype.annotation.Handler;
import com.zutubi.pulse.prototype.record.RecordTypeInfo;
import com.zutubi.pulse.prototype.record.RecordTypeRegistry;
import com.zutubi.pulse.prototype.record.SimpleRecordPropertyInfo;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class FormDescriptorFactory
{
    private RecordTypeRegistry typeRegistry;

    public FormDescriptor createDescriptor(String symbolicName) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        RecordTypeInfo typeInfo = typeRegistry.getInfo(symbolicName);
        return createDescriptor(typeInfo);
    }

    public FormDescriptor createDescriptor(Class type) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        RecordTypeInfo typeInfo = typeRegistry.getInfo(type);
        return createDescriptor(typeInfo);
    }

    public FormDescriptor createDescriptor(RecordTypeInfo typeInfo) throws IllegalAccessException, InstantiationException, IntrospectionException
    {
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setType(typeInfo);

        List<Annotation> annotations = typeInfo.getAnnotations();
        handleAnnotations(descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(typeInfo));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(RecordTypeInfo typeInfo) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (SimpleRecordPropertyInfo propertyInfo : typeInfo.getSimpleInfos())
        {
            FieldDescriptor fieldDescriptor = new FieldDescriptor();
            fieldDescriptor.setName(propertyInfo.getName());

            handleAnnotations(fieldDescriptor, propertyInfo.getAnnotations());

            fieldDescriptors.add(fieldDescriptor);
        }

        return fieldDescriptors;
    }

    private void handleAnnotations(Descriptor descriptor, List<Annotation> annotations) throws IntrospectionException, IllegalAccessException, InstantiationException
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
                AnnotationHandler handler = handlerAnnotation.value().newInstance();
                handler.process(annotation, descriptor);
            }
        }
    }

    /**
     * Required resource
     *
     * @param typeRegistry instance.
     */
    public void setTypeRegistry(RecordTypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
