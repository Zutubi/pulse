package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Using the Constraint annotation, you can associate a {@code com.zutubi.validation.Validator}
 * implementation with other annotations or object properties.
 * <p>
 * When annotating a object property, the validator is used to validate that properties value.  For
 * example, the following code shows the RequiredValidator being explicitly associated with the name
 * property.  This will ensure that validation is successful only if the name property has a value.
 * <code>
 *     @Constraint('com.zutubi.validation.validators.RequiredValidator')
 *     public String getName()
 *     {
 *         return name;
 *     }
 * </code>
 * <p>
 * Similarly, the Constraint annotation can be applied to another annotation.  For example, the following
 * code shows the same constraint used above associated with the Required annotation.  This means that any
 * property that is annotated with the Requried annotation will be validated by the
 * {@code com.zutubi.validation.validators.RequiredValidator}.
 * <code>
 * @Constraint('com.zutubi.validation.validators.RequiredValidator')
 * public @interface Required
 * {
 * }
 *
 * @Required
 * public String getName()
 * {
 *     return name;
 * }
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Constraint
{
    /**
     * The constraint annotations value represents the fully qualified class
     * name of the {@code com.zutubi.validation.Validator} defining this constraint.
     *
     * @return fully qualified class name.
     */
    String[] value();
}
