package com.zutubi.pulse.form.descriptor.annotation;

import com.zutubi.pulse.form.bean.BeanSupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class AbstractAnnotationHandler
{
    /**
     * This method will check if an annotation property has a default value by checking
     * if there is a field named DEFAULT_ + property name.
     *
     * @param annotation
     * @param annotationMethod
     *
     */
    protected boolean isDefault(Annotation annotation, Method annotationMethod)
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
    protected void setPropertiesFromAnnotation(Annotation annotation, Object target)
    {
        for (Method annotationMethod : annotation.getClass().getMethods())
        {
            try
            {
                if (!isDefault(annotation, annotationMethod))
                {
                    BeanSupport.setProperty(
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

    protected Map<String, Object> collectPropertiesFromAnnotation(Annotation annotation)
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        for (Method annotationMethod : annotation.getClass().getMethods())
        {
            try
            {
                if (!isDefault(annotation, annotationMethod))
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
}
