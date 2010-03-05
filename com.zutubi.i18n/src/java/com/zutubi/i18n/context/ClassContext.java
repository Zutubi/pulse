package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * Implementation of the context interface that is based on a class file.
 */
public class ClassContext implements Context
{
    private final Class context;

    public ClassContext(Class context)
    {
        this.context = context;
    }

    public ClassContext(Object context)
    {
        this.context = getClass(context);
    }

    private Class getClass(Object context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("No context specified.");
        }
        if (context instanceof Class)
        {
            return (Class) context;
        }
        else
        {
            return context.getClass();
        }
    }

    public Class getContext()
    {
        return context;
    }

    public InputStream getResourceAsStream(String name)
    {
        return context.getResourceAsStream(name);
    }

    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof ClassContext))
        {
            return false;
        }
        ClassContext otherContext = (ClassContext) other;
        return context.equals(otherContext.context);
    }

    public int hashCode()
    {
        return context.hashCode();
    }

    public String toString()
    {
        return "<" + context.getName() + ">";
    }
}
