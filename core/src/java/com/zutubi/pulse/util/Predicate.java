/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util;

/**
 * Simple binary predicate.
 */
public interface Predicate<T>
{
    boolean satisfied(T t);
}
