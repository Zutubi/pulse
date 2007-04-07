package com.zutubi.prototype.annotation;

import com.zutubi.prototype.Descriptor;
import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.FieldDescriptor;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Collection;

/**
 *
 */
public class SelectAnnotationHandler extends OptionAnnotationHandler
{
    protected Class<? extends OptionProvider> getOptionProviderClass(Annotation annotation)
    {
        return ((Select)annotation).optionProvider();
    }
}
