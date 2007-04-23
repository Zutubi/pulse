package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.scm.ScmException;

/**
 */
public abstract class PerforceErrorDetectingHandler implements PerforceHandler
{
    private boolean throwOnStderr = false;
    private StringBuffer stderr;

    public PerforceErrorDetectingHandler(boolean throwOnStderr)
    {
        this.throwOnStderr = throwOnStderr;
        stderr = new StringBuffer();
    }

    public void handleStderr(String line)
    {
        stderr.append(line);
        stderr.append('\n');
    }

    public void handleExitCode(int code) throws ScmException
    {
        if (code != 0)
        {
            String message = "p4 process returned non-zero exit code: " + Integer.toString(code);

            if (stderr.length() > 0)
            {
                message += ", error '" + stderr.toString().trim() + "'";
            }

            throw new ScmException(message);
        }

        if (stderr.length() > 0 && throwOnStderr)
        {
            throw new ScmException("p4 process returned error '" + stderr.toString().trim() + "'");
        }
    }

    public StringBuffer getStderr()
    {
        return stderr;
    }
}
