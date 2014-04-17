package com.zutubi.tove.variables;

import com.zutubi.tove.variables.api.Variable;

/**
 * A simple variable that can have any type as its value.
 */
public class SimpleVariable<T> implements Variable
{
    private String name;
    private T value;

    public SimpleVariable(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public T getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SimpleVariable that = (SimpleVariable) o;
        if (!name.equals(that.name))
        {
            return false;
        }

        return value.equals(that.value);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return name + "->" + value;
    }
}
