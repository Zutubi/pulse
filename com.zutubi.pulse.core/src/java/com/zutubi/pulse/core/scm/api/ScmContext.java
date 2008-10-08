package com.zutubi.pulse.core.scm.api;

import java.io.File;

/**
 * The context in which SCM operations executed outside of the build are run.
 */
public interface ScmContext
{
    /**
     * @return a persistent working directory available for use by the scm
     * implementation to persist data between invocations.
     */
    File getPersistentWorkingDir();
}
