package com.zutubi.prototype.annotation;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.FieldDescriptor;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Handler for processing reference properties.  Adds the list to select from
 * to the descriptor.
 */
public class ReferenceAnnotationHandler extends OptionAnnotationHandler
{
    protected Class<? extends OptionProvider> getOptionProviderClass(Annotation annotation)
    {
        return ((Reference)annotation).optionProvider();
    }
}
