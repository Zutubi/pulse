package com.zutubi.tove.variables.api;

/**
 * Types that define the variable interface can be referenced in certain
 * configuration using the syntax $(name), where name is the value returned by
 * the getName method.
 *
 * @see com.zutubi.tove.variables.VariableResolver
 */
public interface Variable
{
    String getName();
    Object getValue();
}
