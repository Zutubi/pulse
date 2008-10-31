package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.ScmException;

/**
 * The base exception for git related exception.
 */
public class GitException extends ScmException
{
    public GitException(String errorMessage)
    {
        super(errorMessage);
    }

    public GitException()
    {
    }

    public GitException(Throwable cause)
    {
        super(cause);
    }

    public GitException(String errorMessage, Throwable cause)
    {
        super(errorMessage, cause);
    }
}