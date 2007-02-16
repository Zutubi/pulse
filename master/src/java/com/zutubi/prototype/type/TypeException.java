package com.zutubi.prototype.type;

/**
 *
 *
 */
public class TypeException extends Exception
{
    public TypeException()
    {
    }

    public TypeException(String message)
    {
        super(message);
    }

    public TypeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TypeException(Throwable cause)
    {
        super(cause);
    }
}
