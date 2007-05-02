package com.zutubi.config.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A checkbox field that controls dependent fields.  When the checkbox is in
 * a certain state, dependent fields will be disabled and ignored during
 * validation.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.CONTROLLING_CHECKBOX)
@Constraint("com.zutubi.pulse.validation.validators.ControllingCheckboxValidator")
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface ControllingCheckbox
{
    boolean invert() default false;

    String[] dependentFields() default {};
}
