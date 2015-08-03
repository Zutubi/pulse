package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.tove.model.Descriptor;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;

import java.lang.annotation.Annotation;

/**
 * Handlers are used to take information from annotations and apply them to fields. This allows
 * fields to be configured by annotating the corresponding property without the form building code
 * knowing the details.
 */
public interface AnnotationHandler
{
    // FIXME kendo old version, to be removed.
    void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception;

    void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field) throws Exception;
}
