package com.zutubi.pulse.personal;

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
