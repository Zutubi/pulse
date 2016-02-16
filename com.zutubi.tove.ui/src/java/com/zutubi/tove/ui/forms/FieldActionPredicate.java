package com.zutubi.tove.ui.forms;

import com.zutubi.tove.annotations.FieldAction;
import com.zutubi.tove.ui.model.forms.FieldModel;

/**
 * Determines if a field action should be displayed for a given field.
 */
public interface FieldActionPredicate
{
    boolean satisfied(FieldModel field, FieldAction annotation, FormContext context);
}
