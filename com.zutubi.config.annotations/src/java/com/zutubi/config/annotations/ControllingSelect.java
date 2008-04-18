package com.zutubi.config.annotations;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A select field that controls dependent fields.  When the selected value is
 * <b>not</b> in a given set, dependent fields will be disabled and ignored
 * during validation.  If no dependent fields are specified, the select box
 * controls all other fields in the form.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Field(type = FieldType.CONTROLLING_SELECT)
@Constraint("com.zutubi.pulse.validation.validators.ControllingSelectValidator")
@Handler(className = DefaultAnnotationHandlers.CONTROLLING_SELECT)
public @interface ControllingSelect
{
    String optionProvider() default "";

    String[] enableSet() default {};

    String[] dependentFields() default {};
}
