package com.zutubi.prototype.form;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

/**
 *
 *
 */
public class AnnotationUtils
{
    public static List<Annotation> annotationsFromField(PropertyDescriptor property) throws IntrospectionException
    {
        List<Annotation> annotations = new LinkedList<Annotation>();

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

        // get the field definition as well if it exists?

        return annotations;
    }

}
