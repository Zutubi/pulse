package com.zutubi.prototype.transaction;

/**
 *
 *
 */
//Question: should this be a runtime exception??
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
