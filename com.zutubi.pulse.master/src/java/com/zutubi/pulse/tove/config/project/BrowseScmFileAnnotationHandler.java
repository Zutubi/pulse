package com.zutubi.pulse.tove.config.project;

import com.zutubi.tove.Descriptor;
import com.zutubi.tove.FieldDescriptor;
import com.zutubi.tove.handler.AnnotationHandler;
import com.zutubi.tove.type.CompositeType;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.pulse.tove.config.project.BrowseScmFileAction} annotation.
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
