package com.zutubi.pulse.personal;

import com.zutubi.pulse.core.PulseException;

/**
 */
public class PersonalBuildException extends PulseException
{
    public PersonalBuildException()
    {
    }

    public PersonalBuildException(String errorMessage)
    {
        super(errorMessage);
    }

    public PersonalBuildException(Throwable cause)
    {
        super(cause);
    }

    public PersonalBuildException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
