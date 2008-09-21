package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.ExecutionContext;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * An interface for interaction with SCM servers.
 */
public interface ScmClient extends Closeable
{
    /**
     * Must be called to release resources when this client is not longer
     * required.  No other methods may be called after closing.
     */
    void close();

    /**
     * Returns the functionality that this implementation supports, as a set
     * of capabilities.  Each method's documentation indicates the capability
     * that it helps fulfill.  Before calling a method, ensure that it is
     * supported by checking the associated capability.
     *
     * @return a set of operations this implementation is capable of
     */
    Set<ScmCapability> getCapabilities();

    /**
     * Returns a string that uniquely identifies the server itself.  This may
     * include the server address and repository root, for example.  All
     * SCMClient objects talking to the same SCM should return the same id.
     * <p/>
     * Required for all implementations.
     *
     * @return a unique id for the SCM server
     * @throws ScmException on error
     */
    String getUid() throws ScmException;

    /**
     * Returns a summarised form of the location of the source this SCM has
     * been configured to check out.  For example, an subversion URL.
     * <p/>
     * Required for all implementations.
     *
     * @return a summarised form of the source location, fit for human
     *         consumption
     * @throws ScmException on error
     */
    String getLocation() throws ScmException;

    /**
     * Checks out a new working copy to the specified context.  The target directory for this checkout
     * is defined as the ExecutionContext's working directory.
     * <p/>
     * Required for all implementations.
     *
     * @param context  defines the execution context in which the operation is being run.
     * @param revision defines the revision to be checked out.
     * @param handler  if not null, receives notifications of events during the
     * @return the revision of the locally checked out working copy.
     * @throws ScmException on error
     */
    Revision checkout(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException;

    /**
     * Update the working directory to the specified context.  The target directory for this checkout
     * is defined as the ExecutionContext's working directory.
     * <p/>
     * Required for all implementations.  If an incremental update is not
     * possible then an update may be the same as a checkout.
     *
     * @param context  defines the execution context in which the operation is being run
     * @param revision defines the revision the local working directory should be updated to
     * @param handler  if not null, receives notifications of events during the
     * @return the revision updated to.
     * @throws ScmException on error.
     */
    Revision update(ExecutionContext context, Revision revision, ScmEventHandler handler) throws ScmException;

    /**
     * Checks out the specified file at the given revision.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param path     path defining the content to be retrieved.
     * @param revision the revision be checked out or null for the latest
     *                 revision (may be ignored by implementations that do
     *                 not support {@link com.zutubi.pulse.core.scm.ScmCapability#REVISIONS}).
     * @return input stream providing access to the requested content.
     * @throws ScmException on error
     */
    InputStream retrieve(ScmContext context, String path, Revision revision) throws ScmException;

    /**
     * Stores details about the connection to the server to the given
     * location on disk.  These details should capture all information
     * needed to reproduce the same build, and may also include
     * miscellaneous information the is useful to the user.  If there is no
     * additional information to store, this operation may be a no-op.
     * <p/>
     * Required for all implementations (but may be a no-op).
     *
     * @param outputDir location to store files containing the connection
     *                  details
     * @throws ScmException on error obtaining the details
     * @throws IOException  if an I/O error occurs writing the details to disk
     */
    void storeConnectionDetails(File outputDir) throws ScmException, IOException;

    /**
     * Returns the policy for line endings enforced at a client level, if any.
     * <p/>
     * Required for all implementations.
     *
     * @param context  defines the scm context in which the operation is being run
     * @return the EOL policy, which will be EOLStyle.BINARY if no policy is
     *         in effect
     * @throws ScmException on error
     */
    FileStatus.EOLStyle getEOLPolicy(ScmContext context) throws ScmException;

    /**
     * Returns the latest repository revision.
     * <p/>
     * Required for implementations that support
     * {@link ScmCapability#REVISIONS}.
     *
     * @param context  defines the scm context in which the operation is being run
     * @return the latest revision in the repository
     * @throws ScmException on error
     */
    Revision getLatestRevision(ScmContext context) throws ScmException;

    /**
     * Returns a list of revisions occuring between the given revisions.
     * The from revision itself it NOT included in the result.
     * <p/>
     * Required for {@link ScmCapability#REVISIONS}.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param from     the revision before the first revision to return
     * @param to       the revision that defined the inclusive upper bound for this call.
     *
     * @return a list of revisions for all changes since from
     * @throws ScmException if an error occurs talking to the server
     */
    List<Revision> getRevisions(ScmContext context, Revision from, Revision to) throws ScmException;

    /**
     * Returns a list of changelists occuring in between the given revisions.
     * The changelist that created the from revision itself is NOT included in
     * the model.
     * <p/>
     * Required for {@link ScmCapability#CHANGESETS}.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param from     the revision before the first changelist to include in the model
     * @param to       the last revision to include in the model
     *
     * @return a list of changelists that occured between the two revisions
     *
     * @throws ScmException if an error occurs talking to the server
     */
    List<Changelist> getChanges(ScmContext context, Revision from, Revision to) throws ScmException;

    /**
     * Returns a list of all files/directories in the given path (which
     * should specify a directory).  This function is NOT recursive, i.e.
     * only direct descendents should be listed.
     * <p/>
     * Required for {@link ScmCapability#BROWSE}.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param path     the path to list (relative to the root of the connection,
     *                 i.e. an empty string is valid and means "list the root").
     * @param revision revision at which to browse, or null for the latest
     *                 revision (may be ignored by implementations that do not
     *                 support {@link com.zutubi.pulse.core.scm.ScmCapability#REVISIONS}).
     * @return a list of files and directories contained within the given
     *         path
     * @throws ScmException on error
     */
    List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException;

    /**
     * Applies a tag to the given revision of all files in the server's view .
     * <p/>
     * Required for {@link ScmCapability#TAG}.
     *
     * @param context      defines the execution context in which the operation is being run
     * @param revision     the revision to be tagged
     * @param name         the name of the tag, which has an SCM-specific format
     * @param moveExisting if true and a tag of the same name already exists,
     *                     that tag will be moved to the new revision and files
     *
     * @throws ScmException on error
     */
    void tag(ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException;

    /**
     * Converts a string into a revision.  The string is input from the user,
     * and thus should be validated.  If it is invalid, an SCMException
     * should be thrown.
     * <p/>
     * Required for {@link ScmCapability#REVISIONS}.
     *
     * @param revision revision input string to be converted into an actual
     *                 revision
     * @return a valid revision derived from the string
     * @throws ScmException if the given revision is invalid
     */
    Revision parseRevision(String revision) throws ScmException;
}
