package com.zutubi.tove.handler;

import com.zutubi.tove.Descriptor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 *
 *
 */
public class FieldAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        // Collect all of the annotations fields in a map and add them to the descriptor.
        Map<String, Object> map = AnnotationUtils.collectPropertiesFromAnnotation(annotation);
        for(Map.Entry<String, Object> entry: map.entrySet())
        {
            if(!entry.getKey().equals("type"))
            {
                descriptor.addParameter(entry.getKey(), entry.getValue());
            }
        }
    }
}
