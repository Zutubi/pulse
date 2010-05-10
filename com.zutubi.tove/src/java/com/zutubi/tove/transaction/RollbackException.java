package com.zutubi.tove.transaction;

/**
 * A rollback exception is thrown if an attempt is made to commit
 * a transaction but it fails, forcing a rollback.  After the rollback
 * has been completed, this exception is thrown.
 */
public class RollbackException extends TransactionException
{
    public RollbackException()
    {
    }

    public RollbackException(String message)
    {
        super(message);
    }

    public RollbackException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RollbackException(Throwable cause)
    {
        super(cause);
    }
}