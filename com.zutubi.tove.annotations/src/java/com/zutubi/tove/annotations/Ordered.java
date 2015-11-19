package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a collection-typed property as ordered: i.e. the user can edit the
 * ordering of items in the collection.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ordered
{
    /**
     * @return true if the user should be allowed to reorder selected items,
     *         false if the order is not important (in this case items are
     *         sorted for convenience)
     */
    boolean allowReordering() default true;
}
