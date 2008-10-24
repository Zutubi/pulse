package com.zutubi.pulse.master.tove.handler;

import com.zutubi.config.annotations.Reference;

import java.lang.annotation.Annotation;

/**
 * Handler for processing reference properties.  Adds the list to select from
 * to the descriptor.
 */
public class ReferenceAnnotationHandler extends OptionAnnotationHandler
{
    protected String getOptionProviderClass(Annotation annotation)
    {
        return ((Reference)annotation).optionProvider();
    }
}
