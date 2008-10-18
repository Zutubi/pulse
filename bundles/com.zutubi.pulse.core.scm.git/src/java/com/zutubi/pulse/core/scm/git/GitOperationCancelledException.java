package com.zutubi.pulse.core.scm.git;

public class GitOperationCancelledException extends GitException
{
    public GitOperationCancelledException(String errorMessage)
    {
        super(errorMessage);
    }

    public GitOperationCancelledException()
    {
    }

    public GitOperationCancelledException(Throwable cause)
    {
        super(cause);
    }

    public GitOperationCancelledException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}
