package com.zutubi.pulse.transfer;

/**
 * This exception a specific subset of transfer exceptions, directly related to the
 * jdbc resource.
 *
 */
public class JDBCTransferException extends TransferException
{
    public JDBCTransferException()
    {
    }

    public JDBCTransferException(String message)
    {
        super(message);
    }

    public JDBCTransferException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JDBCTransferException(Throwable cause)
    {
        super(cause);
    }
}
