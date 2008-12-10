package com.zutubi.util.bean;

public class BeanException extends Exception
{
    public BeanException()
    {
    }

    public BeanException(String message)
    {
        super(message);
    }

    public BeanException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BeanException(Throwable cause)
    {
        super(cause);
    }
}
