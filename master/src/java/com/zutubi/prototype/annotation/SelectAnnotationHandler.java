package com.zutubi.prototype.annotation;

import com.zutubi.prototype.OptionProvider;

import java.lang.annotation.Annotation;

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
