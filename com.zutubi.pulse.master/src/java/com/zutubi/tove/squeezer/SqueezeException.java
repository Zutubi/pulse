package com.zutubi.tove.squeezer;

/**
 * <class-comment/>
 */
public class SqueezeException extends Exception
{
    public SqueezeException()
    {
    }

    public SqueezeException(String message)
    {
        super(message);
    }

    public SqueezeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SqueezeException(Throwable cause)
    {
        super(cause);
    }
}
