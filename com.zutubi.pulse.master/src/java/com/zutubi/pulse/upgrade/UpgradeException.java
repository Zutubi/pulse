package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.master.monitor.TaskException;

/**
 * <class-comment/>
 */
public class UpgradeException extends TaskException
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
