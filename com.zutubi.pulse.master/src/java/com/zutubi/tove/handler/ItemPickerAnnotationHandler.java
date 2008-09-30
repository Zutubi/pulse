package com.zutubi.tove.handler;

import com.zutubi.config.annotations.ItemPicker;

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
