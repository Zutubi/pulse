package com.zutubi.tove.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The read only annotation marks the field as read only, preventing it from
 * being modifiable via the UI.  Note that this does not stop it from being
 * programatically modified.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly
{
}
