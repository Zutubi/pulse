package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate action methods to indicate a custom permission name.  By
 * default, a user can perform an action if they have access to write to the
 * path being acted on. Using this annotation a custom permission can be
 * applied, which may be applied to multiple actions.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission
{
    String value();
}
