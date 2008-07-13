package com.zutubi.tove.handler;

import com.zutubi.config.annotations.FieldAction;
import com.zutubi.tove.FieldDescriptor;

/**
 * Determines if a field action should be displayed for a given field.
 */
public interface FieldActionPredicate
{
    boolean satisfied(FieldDescriptor field, FieldAction annotation);
}
