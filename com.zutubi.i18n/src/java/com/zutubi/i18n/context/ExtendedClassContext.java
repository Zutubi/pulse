package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class ExtendedClassContext implements Context
{
    private final Class context;

    public ExtendedClassContext(Class context)
    {
        this.context = context;
    }

    public ExtendedClassContext(Object context)
    {
        this.context = getClass(context);
    }

    private Class getClass(Object context)
    {
        if (context == null)
        {
            throw new IllegalArgumentException();
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
        if (!(other instanceof ExtendedClassContext))
        {
            return false;
        }
        ExtendedClassContext otherContext = (ExtendedClassContext) other;
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
