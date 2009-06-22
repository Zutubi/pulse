package com.zutubi.pulse.core.scm.api;

import java.io.File;

/**
 * An interface for interaction with a checked-out working copy of a project.
 * This interface must be implemented to support personal builds for an SCM.
 * <p/>
 * Implementations of this interface should also consider using the
 * {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI} instance provided by
 * the working copy context to enable querying and/or providing feedback to
 * the user during working copy operations.
 */
public interface WorkingCopy
{
    /**
     * Used to test if the working copy matches the SCM location of a Pulse
     * project.  When the user requests a personal build of a project, some
     * sanity checks should be made to ensure that their working copy is from
     * the same project.  This check is made prior to querying the status of
     * the working copy.
     * <p/>
     * A valid implementation of this method can always return true.  Generally
     * speaking, however, the more verification can be done the better, to
     * provide the user with timely and accurate feedback.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param location the location string for the SCM configured for the Pulse
     *                 project - this is the value returned from {@link ScmClient#getLocation()}
     *                 using the ScmClient implementation for that project
     * @return true if the working copy appears to match the SCM project
     *         configuration identified by the location
     * @throws ScmException on error
     */
    boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException;

    /**
     * Returns the latest revision committed to the remote repository that
     * affects this working copy (i.e. later revisions affecting only files
     * outside the scope of this working copy are not reported).
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @return the latest revision within to the remote repository
     * @throws ScmException on error
     */
    Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException;

    /**
     * Returns a best guess of the have revision for this working copy.  This
     * is the latest revision that the working copy has been updated to.  Note
     * that for some SCMs this can be difficult to determine - and indeed
     * impossible if the workspace has a mixture of revisions.  If no
     * reasonable guess can be made, the implementation may just throw an
     * {@link ScmException}.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @return a best guess of the last revision this workspace was updated to
     * @throws ScmException on error, including when no reasonable guess is
     *                      possible
     */
    Revision guessHaveRevision(WorkingCopyContext context) throws ScmException;

    /**
     * Updates the working copy to the revision specified by synchronising with
     * changes in the SCM server.
     * <p/>
     * As this operation may take some time, implementations should generally
     * use a {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI} to report
     * feedback for an operation in progress.  The UI may also be used to query
     * for user input if necessary.  Implementations are encouraged to report
     * feedback in the format used by the SCM tools themselves.
     *
     * @param context  the context in which the operation is run, in particular
     *                 contains the base directory
     * @param revision the revision to update to, which may be null to indicate
     *                 that the update should be to the latest revision
     * @return the revision which was actually updated to, which may not be
     *         null - if the latest revision is requested the implementation
     *         must return the actual revision that turned out to be
     * @throws ScmException on error
     */
    Revision update(WorkingCopyContext context, Revision revision) throws ScmException;

    /**
     * Creates a patch file representing local changes to the working copy.
     * The patch file should contain enough information to:
     * <ul>
     *   <li>Replicate the local changes in another working copy (at the same
     *       base revision).</li>
     *   <li>Indicate what type of changes have been made, as a
     *       {@link FileStatus} entry for each changed file.</li>
     *   <li>Where possible, provide diffs in unified format for each changed
     *       file (edited text files only, not added files, binary files and so
     *       on).</li>
     * </ul>
     * Where the SCM has a native patch file format that fits these
     * requirements, it should be preferred.  Otherwise, Pulse provides a
     * standard format that can be produced using the supporting class
     * {@link StandardPatchFileSupport}.  To produce standard patch files, the
     * implementation should implement {@link WorkingCopyStatusBuilder}, and
     * forward this method to {@link StandardPatchFileSupport#writePatchFile(WorkingCopyStatusBuilder, WorkingCopyContext, java.io.File, String[])}.
     * <p/>
     * While determining the changes and creating the patch, the
     * implementation is encouraged to output status messages via the UI
     * available in the context.  For example, a line for each changed file and
     * its status could be written as they are discovered.
     * <p/>
     * If the working copy is in an inconsistent state, for example there are
     * unresolved merge conflicts, then this method should report the error
     * using the UI available in the context and return false.
     * <p/>
     * If there are no changes to the local working copy, the implementation
     * should report this using the UI available in the context and return
     * false.
     *
     * @param context   the context in which the operation is run, in particular
     *                  contains the base directory
     * @param patchFile the file to write the patch to
     * @param scope    implementation-defined specification of the scope of the
     *                 operation.  If empty, the operation should apply to the
     *                 entire working copy.  A common implementation will use
     *                 any provided strings as paths of files/directories to be
     *                 included in the operation.  If an SCM provides other
     *                 ways of specifying a scope (e.g. pending changelists),
     *                 the implementation may define and implement a way to
     *                 specify such scopes.  Generally the scope should be
     *                 designed to emulate the behaviour of other tools for the
     *                 SCM.
     * @return true if the working copy was in a consistent state, has some
     *         local changes and a patch was created successfully, false if
     *         any one of these things is not true
     * @throws ScmException on any error creating the patch
     */
    boolean writePatchFile(WorkingCopyContext context, File patchFile, String... scope) throws ScmException;

}
