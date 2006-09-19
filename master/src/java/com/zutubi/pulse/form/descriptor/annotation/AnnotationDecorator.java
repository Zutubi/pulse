package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.descriptor.DescriptorDecorator;
import com.zutubi.pulse.form.descriptor.Descriptor;
import com.zutubi.validation.bean.BeanUtils;
import com.zutubi.validation.bean.BeanException;
import com.zutubi.validation.bean.AnnotationUtils;
import com.zutubi.validation.annotations.Required;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class AnnotationDecorator implements DescriptorDecorator
{
    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        Annotation[] classAnnotations = descriptor.getType().getAnnotations();

        // modify the descriptor based on the annotations located at the class level.
        // (form descriptor...)
        decorateFromAnnotations(descriptor, classAnnotations);

        // now to decorate the individual fields.
        for (FieldDescriptor fieldDescriptor : descriptor.getFieldDescriptors())
        {
            String fieldName = fieldDescriptor.getName();

            // get the annotations for this field.
            List<Annotation> fieldAnnotations = annotationsFromField(fieldName, descriptor.getType());
            decorateFromAnnotations(fieldDescriptor, fieldAnnotations);
        }

        return descriptor;
    }

    private List<Annotation> annotationsFromField(String fieldName, Class type)
    {
        List<Annotation> annotations = new LinkedList<Annotation>();
        try
        {
            PropertyDescriptor property = BeanUtils.getPropertyDescriptor(fieldName, type);
            Method readMethod = property.getReadMethod();
            if (readMethod != null)
            {
                annotations.addAll(Arrays.asList(readMethod.getAnnotations()));
            }
            Method writeMethod = property.getWriteMethod();
            if (writeMethod != null)
            {
                annotations.addAll(Arrays.asList(writeMethod.getAnnotations()));
            }
        }
        catch (BeanException e)
        {
            // noops.
        }
        return annotations;
    }

    private void decorateFromAnnotations(FormDescriptor descriptor, Annotation[] classAnnotations)
    {
        for (Annotation annotation : classAnnotations)
        {
            if (annotation instanceof Form)
            {
                // apply annotations..
                AnnotationUtils.setPropertiesFromAnnotation(annotation, descriptor);
            }
        }
    }

    private void decorateFromAnnotations(FieldDescriptor fieldDescriptor, List<Annotation> fieldAnnotations)
    {
        for (Annotation annotation : fieldAnnotations)
        {
            if (annotation instanceof Field)
            {
                handleFieldAnnotation((Field) annotation, fieldDescriptor);
            }
            if (annotation.annotationType().isAnnotationPresent(Field.class))
            {
                Field fieldAnnotation = annotation.annotationType().getAnnotation(Field.class);
                handleFieldAnnotation(fieldAnnotation, fieldDescriptor);

                // process this annotation since it is marked by the Field annotation.
                fieldDescriptor.getParameters().putAll(AnnotationUtils.collectPropertiesFromAnnotation(annotation));

            }
            if (annotation instanceof Required)
            {
                fieldDescriptor.setRequired(true);
            }
        }
    }

    private void handleFieldAnnotation(Field annotation, FieldDescriptor descriptor)
    {
        descriptor.setFieldType(((Field)annotation).fieldType());
    }
}

