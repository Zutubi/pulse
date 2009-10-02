package com.zutubi.tove.variables.api;

/**
 * Exception raised on errors detected by the {@link com.zutubi.tove.variables.VariableResolver}.
 */
public class ResolutionException extends Exception
{
    public ResolutionException(String message)
    {
        super(message);
    }

    public ResolutionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
