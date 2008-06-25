package com.zutubi.pulse.prototype.config.project;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.prototype.handler.AnnotationHandler;
import com.zutubi.prototype.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.pulse.prototype.config.project.BrowseScmFileAction} annotation.
 */
public class BrowseScmFileAnnotationHandler implements AnnotationHandler
{
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        BrowseScmFileAction action = (BrowseScmFileAction) annotation;
        fieldDescriptor.addParameter("baseDirField", action.baseDirField());
    }
}
