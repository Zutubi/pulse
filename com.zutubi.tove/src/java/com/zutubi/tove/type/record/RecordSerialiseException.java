package com.zutubi.tove.type.record;

import com.zutubi.tove.config.ToveRuntimeException;

/**
 * An error serialising or deserialising a record, which is considered fatal
 * to the operation.
 */
public class RecordSerialiseException extends ToveRuntimeException
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
