package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.scm.ScmCancelledException;
import com.zutubi.pulse.scm.ScmException;

/**
 */
public interface PerforceHandler
{
    void handleStdout(String line);
    void handleStderr(String line);
    void handleExitCode(int code) throws ScmException;

    void checkCancelled() throws ScmCancelledException;
}
