package com.zutubi.validation.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * The ConstraintProperty annotation can be used in conjunction with the
 * {@code com.zutubi.validation.annotations.Constraint} to configure the underlying
 * validator.
 * <p>
 * In the following example, the Max annotation is marked with the Constraint annotation
 * and has its value mapped to the validators max property.
 * {code}
 * @Constraint("com.zutubi.validation.validators.NumericValidator")
 * public @interface Max
 * {
 *    @ConstraintProperty("max") int value();
 * }
 * {code}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConstraintProperty
{
    /**
     * The name of the property to which the annotated value will be mapped.
     *
     * @return property name.
     */
    public String value();
}
