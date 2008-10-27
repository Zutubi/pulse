package com.zutubi.tove.type.record;

/**
 * An error serialising or deserialising a record, which is considered fatal
 * to the operation.
 */
public class RecordSerialiseException extends RuntimeException
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
