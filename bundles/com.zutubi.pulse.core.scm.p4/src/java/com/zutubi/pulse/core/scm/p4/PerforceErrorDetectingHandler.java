package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmException;

/**
 * Perforce output handler that detects and reports errors.
 */
public abstract class PerforceErrorDetectingHandler implements PerforceHandler
{
    /**
     * Perforce may report this error during a sync but it does not affect the
     * actual sync itself, so we can continue regardless.
     */
    private static final String ERROR_PROXY_CACHE = "Proxy could not update its cache";

    private boolean throwOnStderr = false;
    private boolean haveSignificantError = false;
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

        if (!line.contains(ERROR_PROXY_CACHE))
        {
            haveSignificantError = true;
        }
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

        if (haveSignificantError && throwOnStderr)
        {
            throw new ScmException("p4 process returned error '" + stderr.toString().trim() + "'");
        }
    }

    public StringBuffer getStderr()
    {
        return stderr;
    }
}
