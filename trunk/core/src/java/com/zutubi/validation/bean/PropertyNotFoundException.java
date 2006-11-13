package com.zutubi.validation.bean;

/**
 * <class-comment/>
 */
public class PropertyNotFoundException extends BeanPropertyException
{
    public PropertyNotFoundException()
    {
    }

    public PropertyNotFoundException(String message)
    {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PropertyNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
