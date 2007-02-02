package com.zutubi.pulse.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class AnnotationUtils
{
    public static List<Annotation> annotationsFromProperty(PropertyDescriptor property) throws IntrospectionException
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

        return annotations;
    }

}
