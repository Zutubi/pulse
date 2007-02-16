package com.zutubi.prototype.type;

/**
 *
 *
 */
public class TypeConversionException extends TypeException
{
    public TypeConversionException()
    {
    }

    public TypeConversionException(String message)
    {
        super(message);
    }

    public TypeConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TypeConversionException(Throwable cause)
    {
        super(cause);
    }
}
