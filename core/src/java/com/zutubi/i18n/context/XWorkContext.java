package com.zutubi.i18n.context;

/**
 * <class-comment/>
 */
public class XWorkContext implements Context
{
    private final Class context;

    public XWorkContext(Class context)
    {
        this.context = context;
    }

    public XWorkContext(Object context)
    {
        this.context = getClass(context);
    }

    private Class getClass(Object context)
    {
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
        return this.context;
    }
}
