package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.util.reflection.AnnotationUtils;

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
