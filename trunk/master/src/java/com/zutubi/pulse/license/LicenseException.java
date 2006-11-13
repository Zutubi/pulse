package com.zutubi.pulse.license;

import com.zutubi.pulse.core.PulseException;

/**
 * The License Exception is the base exception for License related exceptions.
 *
 */
public class LicenseException extends PulseException
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
