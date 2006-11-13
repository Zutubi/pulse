package com.zutubi.pulse.form.persist;

/**
 * <class-comment/>
 */
public class ObjectNotFoundException extends PersistenceException
{
    public ObjectNotFoundException(String errorMessage)
    {
        super(errorMessage);
    }

    public ObjectNotFoundException()
    {
    }

    public ObjectNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public ObjectNotFoundException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
