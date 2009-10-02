package com.zutubi.tove.variables;

import com.zutubi.tove.variables.api.Variable;

/**
 * A simple variable that can have any type as its value.
 */
public class GenericVariable<T> implements Variable
{
    private String name;
    private T value;

    public GenericVariable(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

}
