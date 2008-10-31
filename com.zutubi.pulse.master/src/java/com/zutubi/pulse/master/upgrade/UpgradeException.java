package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.TaskException;

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
