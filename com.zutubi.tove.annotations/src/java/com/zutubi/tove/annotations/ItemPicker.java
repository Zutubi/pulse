package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Item pickers are a type of multi-select box that allow the user to build
 * up a list of selections.  Items are shown in a dropdown box of options.  This
 * is most appropriate when there is a large number of possible options from
 * which multiple items are selected.
 */
@Target({ ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.ITEM_PICKER)
@Handler(className = DefaultAnnotationHandlers.ITEM_PICKER)
public @interface ItemPicker
{
    String optionProvider();
}
