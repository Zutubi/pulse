package com.zutubi.pulse.master.tove.handler;

import com.zutubi.config.annotations.Select;

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
