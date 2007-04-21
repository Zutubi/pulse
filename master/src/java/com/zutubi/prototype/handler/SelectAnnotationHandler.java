package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.annotation.Select;

import java.lang.annotation.Annotation;

/**
 *
 */
public class SelectAnnotationHandler extends OptionAnnotationHandler
{
    protected String getOptionProviderClass(Annotation annotation)
    {
        return ((Select)annotation).optionProvider();
    }
}
