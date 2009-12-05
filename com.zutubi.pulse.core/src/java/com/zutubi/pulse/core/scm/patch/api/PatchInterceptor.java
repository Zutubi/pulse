package com.zutubi.pulse.core.scm.patch.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.ScmException;

import java.util.List;

/**
 * Optional interface that may be implemented by {@link com.zutubi.pulse.core.scm.api.ScmClient}s
 * to inject extra logic around the application of standard patch files.  Only
 * used when the SCM's working copy implementation returns a patch in standard
 * format (usually by implementing {@link WorkingCopyStatusBuilder}).
 */
public interface PatchInterceptor
{
    /**
     * Called just before the patch file is going to be applied to the working
     * copy that has been bootstrapped for the build.
     *
     * @param context  execution context for the build
     * @param statuses file statuses contained within the patch file
     * @throws ScmException on any error
     */
    void beforePatch(ExecutionContext context, List<FileStatus> statuses) throws ScmException;

    /**
     * Called just after the patch file has been applied to the working copy
     * that has been bootstrapped for the build.
     *
     * @param context  execution context for the build
     * @param statuses file statuses contained within the patch file
     * @throws ScmException on any error
     */
    void afterPatch(ExecutionContext context, List<FileStatus> statuses);
}
