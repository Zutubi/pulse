package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.FieldScript;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.util.TextUtils;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link @com.zutubi.config.annotations.FieldScript} annotation.
 */
public class FieldScriptAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        FieldScript fieldScript = (FieldScript) annotation;
        String template = fieldScript.template();
        if(!TextUtils.stringSet(template))
        {
            FieldDescriptor field = (FieldDescriptor) descriptor;
            template = annotatedType.getClazz().getSimpleName() + "." + field.getProperty().getName();
        }

        fieldDescriptor.addScript(template);
    }
}
