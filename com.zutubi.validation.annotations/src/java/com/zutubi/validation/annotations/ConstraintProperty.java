package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ConstraintProperty annotation can be used in conjunction with the
 * {@code com.zutubi.validation.annotations.Constraint} to configure the underlying
 * validator.
 * <p>
 * In the following example, the Max annotation is marked with the Constraint annotation
 * and has its value mapped to the validator's max property.
 *
 * <pre><code> &#064;Constraint("com.zutubi.validation.validators.NumericValidator")
 * public &#064;interface Max
 * {
 *    &#064;ConstraintProperty("max") int value();
 * }</code></pre>
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
