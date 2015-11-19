package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.tove.handler.AnnotationHandler;
import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * Handler for the {@link com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction} annotation.
 */
public class BrowseScmFileAnnotationHandler implements AnnotationHandler
{
    private static final String PARAMETER_BASE_DIR = "baseDirField";

    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception
    {
        FieldDescriptor fieldDescriptor = (FieldDescriptor) descriptor;
        BrowseScmFileAction action = (BrowseScmFileAction) annotation;
        if (StringUtils.stringSet(action.baseDirField()))
        {
            fieldDescriptor.addParameter(PARAMETER_BASE_DIR, action.baseDirField());
        }
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        BrowseScmFileAction action = (BrowseScmFileAction) annotation;
        if (StringUtils.stringSet(action.baseDirField()))
        {
            field.addParameter(PARAMETER_BASE_DIR, action.baseDirField());
        }
    }
}
