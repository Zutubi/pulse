package com.zutubi.pulse.master.license;

import com.zutubi.pulse.core.api.PulseRuntimeException;

/**
 * The License Exception is the base exception for License related exceptions.
 *
 */
public class LicenseException extends PulseRuntimeException
{
    public LicenseException()
    {
    }

    public LicenseException(String errorMessage)
    {
        super(errorMessage);
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
