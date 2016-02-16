package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.handler.AnnotationHandler;
import com.zutubi.tove.ui.handler.FormContext;
import com.zutubi.tove.ui.model.forms.FieldModel;
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
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        BrowseScmFileAction action = (BrowseScmFileAction) annotation;
        if (StringUtils.stringSet(action.baseDirField()))
        {
            field.addParameter(PARAMETER_BASE_DIR, action.baseDirField());
        }
    }
}
