package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.tove.annotations.FieldScript;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link @com.zutubi.tove.annotations.FieldScript} annotation.
 */
public class FieldScriptAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        FieldScript fieldScript = (FieldScript) annotation;
        String template = fieldScript.template();
        if(!StringUtils.stringSet(template))
        {
            FieldDescriptor field = (FieldDescriptor) descriptor;
            template = annotatedType.getClazz().getSimpleName() + "." + field.getProperty().getName();
        }

        fieldDescriptor.addScript(template);
    }
}
