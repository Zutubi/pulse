package com.zutubi.tove.transaction;

public class TransactionException extends RuntimeException
{
    public TransactionException()
    {
    }

    public TransactionException(String message)
    {
        super(message);
    }

    public TransactionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public TransactionException(Throwable cause)
    {
        super(cause);
    }
}
