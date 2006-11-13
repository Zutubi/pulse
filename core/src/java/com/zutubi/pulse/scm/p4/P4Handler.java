package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.scm.SCMCancelledException;
import com.zutubi.pulse.scm.SCMException;

/**
 */
public interface P4Handler
{
    void handleStdout(String line) throws SCMException;
    void handleStderr(String line) throws SCMException;
    void handleExitCode(int code) throws SCMException;

    void checkCancelled() throws SCMCancelledException;
}
