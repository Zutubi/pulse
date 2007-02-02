package com.zutubi.prototype.form;

import com.zutubi.prototype.form.annotation.AnnotationHandler;
import com.zutubi.prototype.form.annotation.Handler;
import com.zutubi.pulse.util.AnnotationUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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
    public FormDescriptor createDescriptor(Class type) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        FormDescriptor descriptor = new FormDescriptor();
        descriptor.setType(type);
        
        List<Annotation> annotations = Arrays.asList(type.getAnnotations());
        handleAnnotations(descriptor, annotations);

        descriptor.setFieldDescriptors(buildFieldDescriptors(type));

        return descriptor;
    }

    private List<FieldDescriptor> buildFieldDescriptors(Class type) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        List<FieldDescriptor> fieldDescriptors = new LinkedList<FieldDescriptor>();

        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);

        // Handle the first pass analysis.  Here, all of the fields are considered on an individual basis.
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
        {
            FieldDescriptor fieldDescriptor = new FieldDescriptor();
            fieldDescriptor.setName(propertyDescriptor.getName());

            handleAnnotations(fieldDescriptor, propertyDescriptor);

            fieldDescriptors.add(fieldDescriptor);
        }

        return fieldDescriptors;
    }


    private void handleAnnotations(Descriptor descriptor, PropertyDescriptor propertyDescriptor) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        handleAnnotations(descriptor, AnnotationUtils.annotationsFromProperty(propertyDescriptor));
    }

    private void handleAnnotations(Descriptor descriptor, List<Annotation> annotations) throws IntrospectionException, IllegalAccessException, InstantiationException
    {
        // need to recurse over annotations, ignoring the java.lang annotations.
        for (Annotation annotation : annotations)
        {
            if (annotation.annotationType().getName().startsWith("java.lang"))
            {
                // ignore standard annotations.
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
}
