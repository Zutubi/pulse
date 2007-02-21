package com.zutubi.pulse.util;

import com.zutubi.validation.bean.BeanUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

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

    /**
     * This method will check if an annotation property has a default value by checking
     * if there is a field named DEFAULT_ + property name.
     *
     * @param annotation
     * @param annotationMethod
     *
     */
    public static boolean isDefault(Annotation annotation, Method annotationMethod)
    {
        try
        {
            java.lang.reflect.Field defaultField = annotation.getClass().getField("DEFAULT_" + annotationMethod.getName());
            if (defaultField != null)
            {
                if (annotationMethod.invoke(annotation).equals(
                        defaultField.get(annotation)))
                {
                    return true;
                }
            }
            return false;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * For each attribute of annotation, will search for a matching property on
     * the target and set it with the value of the attribute unless the attribute
     * is set to the "default" value
     *
     * @param annotation
     *
     * @param target
     */
    public static void setPropertiesFromAnnotation(Annotation annotation, Object target)
    {
        for (Method annotationMethod : annotation.getClass().getMethods())
        {
            try
            {
                if (!isDefault(annotation, annotationMethod))
                {
                    BeanUtils.setProperty(
                            annotationMethod.getName(),
                            annotationMethod.invoke(annotation),
                            target
                    );
                }

            }
            catch (Exception e)
            {
                // noop.
            }
        }
    }

    public static Map<String, Object> collectPropertiesFromAnnotation(Annotation annotation)
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Method annotationMethod : annotation.getClass().getDeclaredMethods())
        {
            try
            {
                if (isUserDeclared(annotationMethod) && !isDefault(annotation, annotationMethod))
                {
                    properties.put(annotationMethod.getName(), annotationMethod.invoke(annotation));
                }
            }
            catch (Exception e)
            {
                // noop.
            }
        }
        return properties;
    }

    private static final Set<String> internalMethods = new HashSet<String>();
    static
    {
        internalMethods.add("toString");
        internalMethods.add("hashCode");
        internalMethods.add("annotationType");
        internalMethods.add("equals");
    }

    public static boolean isUserDeclared(Method annotationMethod)
    {
        return !internalMethods.contains(annotationMethod.getName());
    }
}
