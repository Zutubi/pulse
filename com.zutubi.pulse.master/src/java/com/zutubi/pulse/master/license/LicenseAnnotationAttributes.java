package com.zutubi.pulse.master.license;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <class-comment/>
 */
public class LicenseAnnotationAttributes
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
                attributes.addAll(Arrays.asList(attr.value()));
                break;
            }
        }
        return attributes;
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
                attributes.addAll(Arrays.asList(attr.value()));
                break;
            }
        }
        return attributes;
    }
}
