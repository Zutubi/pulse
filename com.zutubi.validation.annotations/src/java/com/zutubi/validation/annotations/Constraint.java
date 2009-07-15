package com.zutubi.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Using the Constraint annotation, you can associate a {@link com.zutubi.validation.Validator}
 * implementation with other annotations or object properties.
 * <p>
 * When annotating an object property, the validator is used to validate that property's value.  For
 * example, the following code shows the RequiredValidator being explicitly associated with the name
 * property.  This will ensure that validation is successful only if the name property has a value.
 *
 * <pre><code> &#064;Constraint('com.zutubi.validation.validators.RequiredValidator')
 * public String getName()
 * {
 *     return name;
 * }</code></pre>
 * <p>
 * Similarly, the Constraint annotation can be applied to another annotation.  For example, the following
 * code shows the same constraint used above associated with the Required annotation.  This means that any
 * property that is annotated with the Requried annotation will be validated by the
 * {@link com.zutubi.validation.validators.RequiredValidator}.
 *
 * <pre><code> &#064;Constraint('com.zutubi.validation.validators.RequiredValidator')
 * public &#064;interface Required
 * {
 * }
 *
 * &#064;Required
 * public String getName()
 * {
 *     return name;
 * }</code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Constraint
{
    /**
     * The fully qualified class name of the {@link com.zutubi.validation.Validator} 
     * defining this constraint.
     *
     * @return fully qualified class name.
     */
    String[] value();
}
