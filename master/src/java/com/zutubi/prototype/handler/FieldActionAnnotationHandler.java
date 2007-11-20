package com.zutubi.prototype.handler;

import com.zutubi.util.TextUtils;
import com.zutubi.config.annotations.FieldAction;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.config.annotations.FieldAction} annotation.
 */
public class FieldActionAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        FieldAction fieldAction = (FieldAction) annotation;
        fieldDescriptor.addAction(fieldAction.actionKey());
        if (TextUtils.stringSet(fieldAction.template()))
        {
            fieldDescriptor.addScript(fieldAction.template());
        }
    }
}
