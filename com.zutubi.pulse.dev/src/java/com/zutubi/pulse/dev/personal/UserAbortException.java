package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.PersonalBuildException;

/**
 * A special exception that is thrown when the user chooses to abort a
 * personal build.
 */
public class UserAbortException extends PersonalBuildException
{
    public String getMessage()
    {
        return "Aborted";        
    }
}
