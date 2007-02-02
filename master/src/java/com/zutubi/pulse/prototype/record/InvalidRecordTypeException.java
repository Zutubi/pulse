package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseException;

/**
 */
public class InvalidRecordTypeException extends PulseException
{
    public InvalidRecordTypeException(String errorMessage)
    {
        super(errorMessage);
    }

    public InvalidRecordTypeException()
    {
    }

    public InvalidRecordTypeException(Throwable cause)
    {
        super(cause);
    }

    public InvalidRecordTypeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
