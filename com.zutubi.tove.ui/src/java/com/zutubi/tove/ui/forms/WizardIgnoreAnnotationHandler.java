package com.zutubi.tove.ui.forms;

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.model.forms.FieldModel;

import java.lang.annotation.Annotation;

/**
 * Tags fields for properties marked @Wizard.Ignore with an extended parameter.
 */
public class WizardIgnoreAnnotationHandler implements AnnotationHandler
{
    @Override
    public boolean requiresContext(Annotation annotation)
    {
        return false;
    }

    @Override
    public void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception
    {
        field.addParameter("wizardIgnore", true);
    }
}
