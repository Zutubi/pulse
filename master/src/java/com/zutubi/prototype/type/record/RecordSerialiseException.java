package com.zutubi.prototype.type.record;

import com.zutubi.pulse.core.PulseRuntimeException;

/**
 * An error serialising or deserialising a record, which is considered fatal
 * to the operation.
 */
public class RecordSerialiseException extends PulseRuntimeException
{
    public RecordSerialiseException()
    {
    }

    public RecordSerialiseException(String errorMessage)
    {
        super(errorMessage);
    }

    public RecordSerialiseException(Throwable cause)
    {
        super(cause);
    }

    public RecordSerialiseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
