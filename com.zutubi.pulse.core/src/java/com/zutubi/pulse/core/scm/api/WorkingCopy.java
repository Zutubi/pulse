package com.zutubi.pulse.core.scm.api;

/**
 * An interface for interaction with a checked-out working copy of a project.
 * This interface must be implemented to support personal builds for an SCM.
 * <p/>
 * Implementations of this interface should also consider implementing
 * {@link com.zutubi.pulse.core.scm.api.PersonalBuildUIAware} and using the
 * provided {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI} instance to
 * enable querying and/or providing feedback to the user during working copy
 * operations.
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
     * Reports on the status of a working directory.  Used to determine what
     * local changes have been made to the files in the working copy, so that
     * these changes can be replicated in another workng copy.
     * <p/>
     * The returned status need only report file statuses that are interesting,
     * as defined by {@link FileStatus#isInteresting()}.  Unchanged files may
     * be recorded if it is simpler but generally should not to reduce the size
     * of the returned data.
     * <p/>
     * As this operation may take some time, implementations should generally
     * use a {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI} to report
     * feedback for an operation in progress.  The UI may also be used to query
     * for user input if necessary.  Implementations are encourage to report
     * feedback in the format used by the SCM tools themselves.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param spec    implementation-defined specification of the scope of the
     *                operation.  If empty, the operation should apply to the
     *                entire working copy.  A common implementation will use
     *                any provided strings as paths of files/directories to be
     *                included in the operation.  If an SCM provides other ways
     *                of specifying a scope (e.g. pending changelists), the
     *                implementation may define and implement a way to specify
     *                such scopes.  Generally the scope should be designed to
     *                emulate the behaviour of other tools for the SCM.
     * @return status information containing a file status for at least each
     *         interesting file in the working copy
     * @throws ScmException on error
     */
    WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException;

    /**
     * Updates the working copy to the revision specified be synchronising with
     * changes in the SCM server.
     * <p/>
     * As this operation may take some time, implementations should generally
     * use a {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI} to report
     * feedback for an operation in progress.  The UI may also be used to query
     * for user input if necessary.  Implementations are encourage to report
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
}
