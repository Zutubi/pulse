package com.zutubi.pulse.master.tove.handler;

import com.zutubi.pulse.master.rest.model.forms.FieldModel;
import com.zutubi.pulse.master.tove.model.FieldDescriptor;
import com.zutubi.tove.annotations.FieldAction;

/**
 * Determines if a field action should be displayed for a given field.
 */
public interface FieldActionPredicate
{
    // FIXME kendo old version
    boolean satisfied(FieldDescriptor field, FieldAction annotation);

    boolean satisfied(FieldModel field, FieldAction annotation);
}
