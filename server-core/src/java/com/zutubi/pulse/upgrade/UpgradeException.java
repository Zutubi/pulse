package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.core.BobException;

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
