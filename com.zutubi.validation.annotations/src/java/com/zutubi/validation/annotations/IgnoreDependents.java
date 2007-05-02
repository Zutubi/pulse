package com.zutubi.validation.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <class-comment/>
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
