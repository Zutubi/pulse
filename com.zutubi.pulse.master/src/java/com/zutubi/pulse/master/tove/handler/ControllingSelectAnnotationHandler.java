package com.zutubi.pulse.master.tove.handler;

import com.zutubi.tove.annotations.ControllingSelect;

import java.lang.annotation.Annotation;

/**
 *
 */
public class ControllingSelectAnnotationHandler extends OptionAnnotationHandler
{
    protected String getOptionProviderClass(Annotation annotation)
    {
        return ((ControllingSelect)annotation).optionProvider();
    }
}
