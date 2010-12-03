package com.zutubi.pulse.core.scm.patch.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.scm.api.*;

import java.io.File;
import java.util.List;

/**
 * An interface for the creation and application of patches.  Patches are used
 * in personal builds to transfer local changes to the Pulse master for
 * building before they are submitted.
 */
public interface PatchFormat
{
    /**
     * Creates a patch file representing local changes to a working copy. The patch file should
     * contain enough information to:
     * <ul>
     *   <li>Replicate the local changes in another working copy (at a compatible base revision).</li>
     *   <li>Indicate what type of changes have been made, as a
     *       {@link FileStatus} entry for each changed file.</li>
     *   <li>Where possible, provide diffs in unified format for each changed file (edited text
     *       files only, not added files, binary files and so on).</li>
     * </ul>
     * Where the SCM has a native patch file format that fits these requirements, it should be
     * preferred.  Otherwise, Pulse provides a standard format implementation
     * {@link com.zutubi.pulse.core.scm.patch.StandardPatchFormat} that can be leveraged to do much
     * of the work.  To use the standard implementation the WorkingCopy must implement
     * {@link com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatusBuilder}.
     * <p/>
     * While determining the changes and creating the patch, the implementation is encouraged to
     * output status messages via the UI available in the context.  For example, a line for each
     * changed file and its status could be written as they are discovered.
     * <p/>
     * If the working copy is in an inconsistent state, for example there are unresolved merge
     * conflicts, then this method should report the error using the UI available in the context and
     * return false.
     * <p/>
     * If there are no changes to the local working copy, the implementation should report this
     * using the UI available in the context and return false.
     * <p/>
     * Implementations need not support this method if the patch file can be obtained via other
     * means.  Instead, an {@link UnsupportedOperationException} can be thrown. In that case no SCM
     * plugin should register this patch format as its default - the format will only be used when a
     * patch file created separately by the user is submitted to Pulse.
     *
     * @param workingCopy implementation used to interact with the working copy
     * @param context     the context in which the operation is run, in particular contains the base
     *                    directory
     * @param patchFile   the file to write the patch to
     * @param scope       implementation-defined specification of the scope of the operation.  If
     *                    empty, the operation should apply to the entire working copy.  A common
     *                    implementation will use any provided strings as paths of files/directories
     *                    to be included in the operation.  If an SCM provides other ways of
     *                    specifying a scope (e.g. pending changelists), the implementation may
     *                    define and implement a way to specify such scopes.  Generally the scope
     *                    should be designed to emulate the behaviour of other tools for the SCM.
     * @return true if the working copy was in a consistent state, has some local changes and a
     *         patch was created successfully, false if any one of these things is not true
     * @throws com.zutubi.pulse.core.scm.api.ScmException on any error creating the patch
     */
    boolean writePatchFile(WorkingCopy workingCopy, WorkingCopyContext context, File patchFile, String... scope) throws ScmException;

    /**
     * Applies the changes in the given patch file to a working copy based at the given directory.
     * This patch file would have been created by calling
     * {@link #writePatchFile(WorkingCopy, WorkingCopyContext, java.io.File, String...)}.
     * <p/>
     * Generally speaking the implementation should reject patches that don't apply cleanly.
     * Smaller, recoverable, problems may be reported as warning features in the returned feature
     * list, but any significant problems should cause the patch to fail with an
     * {@link ScmException}.
     * <p/>
     *
     * @param context            context of the build in which the patch is being applied
     * @param patchFile          the patch file to apply
     * @param baseDir            base of the working copy to which the patch should be applied
     * @param scmClient          client for the SCM used to bootstrap this build
     * @param scmFeedbackHandler handler used to report status for long-running operations - the
     *                           implementation should also call {@link com.zutubi.pulse.core.scm.api.ScmFeedbackHandler#checkCancelled()}
     *                           on this handler as frequently as practical
     * @return                   a list of messages from applying the patch, which will be added to
     *                           the command invoking this method.  Use this to report information
     *                           or warnings about recoverable problems applying the patch.
     * @throws ScmException on any error, including problems with clean application of the patch
     */
    List<Feature> applyPatch(ExecutionContext context, File patchFile, File baseDir, ScmClient scmClient, ScmFeedbackHandler scmFeedbackHandler) throws ScmException;

    /**
     * Reads all file status information from the given patch file.  The returned list should
     * contain an entry for every file that is represented in the patch file.
     *
     * @param patchFile the patch file to read the status information from
     * @return a list of status information from the given patch file
     * @throws ScmException on any error
     */
    List<FileStatus> readFileStatuses(File patchFile) throws ScmException;

    /**
     * Tests if a file appears to be a patch in this format.  This is not an exact test, so a valid
     * implementation can always return false.  However, a better guess may lead to more convenience
     * for the user (as it may relieve them from specifying patch formats explicitly).
     *
     * @param patchFile the file to test
     * @return true if this file appears to be a patch in this format.
     */
    boolean isPatchFile(File patchFile);
}
