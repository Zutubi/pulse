package com.zutubi.pulse.license;

import org.springframework.metadata.Attributes;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;

/**
 * <class-comment/>
 */
public class LicenseAnnotationAttributes implements Attributes
{
    public Collection<String> getAttributes(Class target)
    {
        Set<String> attributes = new HashSet<String>();

        for (Annotation annotation : target.getAnnotations())
        {
            // check for Licensed annotations
            if (annotation instanceof Licensed)
            {
                Licensed attr = (Licensed) annotation;
                for (String auth : attr.value())
                {
                    attributes.add(auth);
                }
                break;
            }
        }
        return attributes;
    }

    public Collection getAttributes(Class clazz, Class filter)
    {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    public Collection<String> getAttributes(Method target)
    {
        Set<String> attributes = new HashSet<String>();

        for (Annotation annotation : target.getAnnotations())
        {
            // check for Licensed annotations
            if (annotation instanceof Licensed)
            {
                Licensed attr = (Licensed) annotation;
                for (String auth : attr.value())
                {
                    attributes.add(auth);
                }
                break;
            }
        }
        return attributes;
    }

    public Collection getAttributes(Method method, Class aClass)
    {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    public Collection getAttributes(Field field)
    {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }

    public Collection getAttributes(Field field, Class aClass)
    {
        throw new UnsupportedOperationException("Unsupported Operation.");
    }
}
