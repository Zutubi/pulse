package com.zutubi.pulse.license;

import com.zutubi.pulse.core.PulseException;

/**
 * <class-comment/>
 */
public class LicenseException extends PulseException
{
    public LicenseException(String errorMessage)
    {
        super(errorMessage);
    }

    public LicenseException()
    {
    }

    public LicenseException(Throwable cause)
    {
        super(cause);
    }

    public LicenseException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
