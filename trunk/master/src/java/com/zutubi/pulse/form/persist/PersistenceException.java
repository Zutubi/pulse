package com.zutubi.pulse.form.persist;

import com.zutubi.pulse.core.PulseException;

/**
 * <class-comment/>
 */
public class PersistenceException extends PulseException
{
    public PersistenceException(String errorMessage)
    {
        super(errorMessage);
    }

    public PersistenceException()
    {
    }

    public PersistenceException(Throwable cause)
    {
        super(cause);
    }

    public PersistenceException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
