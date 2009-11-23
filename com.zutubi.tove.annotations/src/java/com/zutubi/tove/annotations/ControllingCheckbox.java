package com.zutubi.tove.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A checkbox field that controls dependent fields.  When the checkbox is in
 * a certain state, dependent fields will be disabled and ignored during
 * validation.  If no dependent fields are specified (i.e. no checked or
 * unchecked fields), all other fields in the form will only be enabled when
 * the box is checked.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.CONTROLLING_CHECKBOX)
@Constraint("com.zutubi.tove.validation.ControllingCheckboxValidator")
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface ControllingCheckbox
{
    /**
     * @return the names of fields that should be enabled when the box is
     *         checked
     */
    String[] checkedFields() default {};
    /**
     * @return the names of fields that should be enabled when the box is
     *         not checked
     */
    String[] uncheckedFields() default {};
}
