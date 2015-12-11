package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.bean.BeanException;
import com.zutubi.util.bean.BeanUtils;
import com.zutubi.util.reflection.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Generic field handling which copies properties from the annotation to the field.
 */
public class FieldAnnotationHandler implements AnnotationHandler
{
    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        Map<String, Object> map = AnnotationUtils.collectPropertiesFromAnnotation(annotation);
        for (Map.Entry<String, Object> entry: map.entrySet())
        {
            String name = entry.getKey();
            if (!name.equals("type"))
            {
                try
                {
                    BeanUtils.setProperty(name, entry.getValue(), field);
                }
                catch (BeanException e)
                {
                    field.addParameter(name, entry.getValue());
                }
            }
        }
    }
}
