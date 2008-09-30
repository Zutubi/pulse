package com.zutubi.pulse.core;

/**
 * A simple reference that can have any type as its value.
 */
public class GenericReference<T> implements Reference
{
    private String name;
    private T value;

    public GenericReference(String name, T value)
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
