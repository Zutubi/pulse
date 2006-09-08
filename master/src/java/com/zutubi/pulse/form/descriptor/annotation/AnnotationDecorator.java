package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.descriptor.decorators.DecoratorSupport;
import com.zutubi.pulse.form.descriptor.FormDescriptor;
import com.zutubi.pulse.form.descriptor.Descriptor;
import com.zutubi.pulse.form.descriptor.FieldDescriptor;
import com.zutubi.pulse.form.bean.BeanSupport;
import com.zutubi.pulse.form.bean.BeanException;

import java.lang.annotation.Annotation;
import java.beans.PropertyDescriptor;

/**
 * <class-comment/>
 */
public class AnnotationDecorator extends DecoratorSupport
{
    public FormDescriptor decorate(FormDescriptor descriptor)
    {
        // decorate using any annotations defined on the class level.
        Annotation[] classAnnotations = descriptor.getType().getAnnotations();
        FormDescriptor decoratedDescriptor = (FormDescriptor)decorateFromAnnotations(descriptor, classAnnotations);

        // now decorate the fields.

        for (FieldDescriptor fd : descriptor.getFieldDescriptors())
        {
            try
            {
                PropertyDescriptor pd = BeanSupport.getPropertyDescriptor(fd.getName(), descriptor.getType());
                decorateFromAnnotations(fd, pd.getReadMethod().getAnnotations());
                decorateFromAnnotations(fd, pd.getWriteMethod().getAnnotations());
            }
            catch (BeanException e)
            {
                e.printStackTrace();
            }
        }

        return decoratedDescriptor;
    }


    private Descriptor decorateFromAnnotations( Descriptor descriptor, Annotation[] annotations)
    {
        for (Annotation annotation : annotations)
        {
            // If the annotation type itself has a DescriptorAnnotation, it's one of ours
            DescriptorAnnotation handlerAnnotation = annotation.annotationType().getAnnotation(DescriptorAnnotation.class);
            if (handlerAnnotation != null)
            {
                try
                {
                    DescriptorAnnotationHandler handler = handlerAnnotation.value().newInstance();
                    descriptor = handler.decorateFromAnnotation(annotation, descriptor);
                }
                catch (Exception ex)
                {
                    //ex.printStackTrace();
                }
            }
        }
        return descriptor;
    }

}
