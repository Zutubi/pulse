package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

/**
 * Allows customisation of the ordering of nested properties when they are
 * listed.  The default is alphabetical listing by display name, but in some
 * cases it makes sense to prioritise differently.
 */
public @interface Listing
{
    /**
     * Specify a custom ordering of nested properties.
     *
     * Any properties not included in this ordering will be ordered
     * alphabetically by display name after the listed properties.
     *
     * @return an array of property names defining the order in which they
     *         are listed
     */
    String[] order();
}
