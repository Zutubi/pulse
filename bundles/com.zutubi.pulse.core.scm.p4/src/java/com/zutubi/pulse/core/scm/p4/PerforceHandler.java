package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;

/**
 */
public interface PerforceHandler
{
    void handleCommandLine(String line);
    void handleStdout(String line);
    void handleStderr(String line);
    void handleExitCode(int code) throws ScmException;

    void checkCancelled() throws ScmCancelledException;
}
