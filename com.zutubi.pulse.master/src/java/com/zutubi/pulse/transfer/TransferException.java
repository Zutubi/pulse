package com.zutubi.pulse.transfer;

/**
 *
 *
 */
public class TransferException extends Exception
{
    public TransferException()
    {
    }

    public TransferException(String message)
    {
        super(message);
    }

    public TransferException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TransferException(Throwable cause)
    {
        super(cause);
    }
}
