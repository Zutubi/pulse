package com.zutubi.pulse.master.tove.handler;

import com.zutubi.tove.annotations.ItemPicker;

import java.lang.annotation.Annotation;

/**
 * Handler that provides the options for an ItemPicker.
 */
public class ItemPickerAnnotationHandler extends OptionAnnotationHandler
{
    protected String getOptionProviderClass(Annotation annotation)
    {
        return ((ItemPicker)annotation).optionProvider();
    }
}
