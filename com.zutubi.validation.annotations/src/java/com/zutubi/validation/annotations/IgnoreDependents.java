package com.zutubi.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The IgnoreDependents annotation should be added to fields that are used
 * to toggle the availability of a set of fields via the interface.  In doing
 * so, when the fields are 'disabled', the validation requirements of those
 * dependent fields can be ignored.
 */
@Constraint("com.zutubi.validation.validators.IgnoreDependentsFieldValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreDependents
{
    /**
     * @return field value which will cause dependents to <b>not</b> be
     *         ignored.  This value will be compared with the value of the
     *         field converted to a string with toString().
     */
    String nonIgnoreValue() default "true";

    String[] dependentFields() default {};
}
