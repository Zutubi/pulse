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
 * <p/>
 * Handlers may be able to work with only static/type information, or may require more context
 * (e.g. the instance being configured).  See {@link #requiresContext(Annotation)} for details.
 */
public interface AnnotationHandler
{
    /**
     * Indicates if this handler can run with only static information about the field, or whether
     * it needs more context information.  If context is not required the handler will process
     * all forms with a null context.  It context is required the handler will only process forms
     * where we know more about what is being configured (e.g. an existing instance).
     *
     * @param annotation annotation instance that links to this handler
     * @return true iff this handler requires context to process a field
     */
    boolean requiresContext(Annotation annotation);

    // FIXME kendo old version, to be removed.
    void process(CompositeType annotatedType, Annotation annotation, Descriptor descriptor) throws Exception;

    void process(CompositeType annotatedType, TypeProperty property, Annotation annotation, FieldModel field, FormContext context) throws Exception;
}
