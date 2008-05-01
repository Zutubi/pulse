package com.zutubi.pulse.transfer.jdbc;

import com.zutubi.pulse.transfer.TransferException;

/**
 * This exception a specific subset of transfer exceptions, directly related to the
 * jdbc resource.
 *
 */
public class HibernateTransferException extends TransferException
{
    public HibernateTransferException()
    {
    }

    public HibernateTransferException(String message)
    {
        super(message);
    }

    public HibernateTransferException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HibernateTransferException(Throwable cause)
    {
        super(cause);
    }
}
