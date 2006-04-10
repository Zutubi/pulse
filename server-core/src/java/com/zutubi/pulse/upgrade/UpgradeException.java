package com.cinnamonbob.upgrade;

import com.cinnamonbob.core.BobException;

/**
 * <class-comment/>
 */
public class UpgradeException extends BobException
{
    public UpgradeException()
    {
    }

    public UpgradeException(Throwable cause)
    {
        super(cause);
    }

    public UpgradeException(String errorMessage)
    {
        super(errorMessage);
    }

    public UpgradeException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
