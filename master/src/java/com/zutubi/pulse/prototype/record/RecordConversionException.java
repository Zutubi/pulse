package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseRuntimeException;

/**
 * Thrown when there is an error converting between records and objects.
 */
public class RecordConversionException extends PulseRuntimeException
{
    public RecordConversionException(String errorMessage)
    {
        super(errorMessage);
    }

    public RecordConversionException()
    {
    }

    public RecordConversionException(Throwable cause)
    {
        super(cause);
    }

    public RecordConversionException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
