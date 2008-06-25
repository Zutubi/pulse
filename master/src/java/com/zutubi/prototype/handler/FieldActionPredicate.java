package com.zutubi.prototype.handler;

import com.zutubi.config.annotations.FieldAction;
import com.zutubi.prototype.FieldDescriptor;

/**
 * Determines if a field action should be displayed for a given field.
 */
public interface FieldActionPredicate
{
    boolean satisfied(FieldDescriptor field, FieldAction annotation);
}
