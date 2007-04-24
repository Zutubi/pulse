package com.zutubi.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

// This annotation is a form field of type PASSWORD.
@Field(type = FieldType.PASSWORD)

/**
 * The password annotation allows you to mark a property for display as a simple form password field.
 * 
 */
@Handler(className = DefaultAnnotationHandlers.FIELD)
public @interface Password
{
    /**
     * Indicates whether or not the contents of the password field should be shown as '*'s.
     *
     * This field defaults to true.
     *
     * @return the true if the password should be shown, false otherwise.
     */
    public boolean showPassword() default true;

}
