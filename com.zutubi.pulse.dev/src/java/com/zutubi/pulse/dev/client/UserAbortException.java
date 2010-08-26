package com.zutubi.pulse.dev.client;

/**
 * A special exception that is thrown when the user chooses to abort a
 * dev command.
 */
public class UserAbortException extends ClientException
{
    public String getMessage()
    {
        return "Aborted";        
    }
}
