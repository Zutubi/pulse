package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmException;

/**
 */
public interface PerforceHandler
{
    void handleStdout(String line);
    void handleStderr(String line);
    void handleExitCode(int code) throws ScmException;

    void checkCancelled() throws ScmCancelledException;
}
