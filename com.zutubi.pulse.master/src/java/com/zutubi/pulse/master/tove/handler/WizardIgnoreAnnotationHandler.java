package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

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
