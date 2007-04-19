package com.zutubi.util.bean;

/**
 * <class-comment/>
 */
public class BeanPropertyException extends BeanException
{
    public BeanPropertyException()
    {
    }

    public BeanPropertyException(String message)
    {
        super(message);
    }

    public BeanPropertyException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BeanPropertyException(Throwable cause)
    {
        super(cause);
    }
}
