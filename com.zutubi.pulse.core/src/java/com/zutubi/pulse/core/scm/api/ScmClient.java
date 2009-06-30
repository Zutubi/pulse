package com.zutubi.pulse.core.scm.api;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;

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
     * The init method is a callback that is called before any of the methods that receive
     * an SCM context.  It is called once when a project is initialised.  It may be called
     * again for the same project if the user chooses to manually reinitialise, in which
     * case the previous SCM persistent directory will have already been cleaned up, but
     * any other external artifacts from an earlier initialisation may still exist.
     * <p/>
     * It is during this callback that any long running tasks to prepare the SCM's
     * persistent working directory (on the master) can be run.
     * <p/>
     * Note that exclusive access to the passed context is guaranteed for the duration
     * of this call.
     * 
     * @param context the scm context that will be used for subsequent calls.
     * @param handler handler for receipt of feedback during long-running initialisation
     *
     * @throws ScmException if there is a problem
     *
     * @see #destroy(ScmContext, ScmFeedbackHandler)
     */
    void init(ScmContext context, ScmFeedbackHandler handler) throws ScmException;

    /**
     * The destroy method is called when a project is deleted or the user manually
     * requests reinitialisation of the project that has previously been initialised
     * successfully.  Destroy is <strong>not</strong> called on reinitialisation if
     * the last initialisation attempt failed.
     * <p/>
     * During this callback the implementation should cleanup any persistent artifacts
     * it stores for the project, particularly those store in an external system such
     * as an external SCM server.
     * <p/>
     * Note that it is not necessary to delete the persistent working directory in
     * the SCM context, as this will be done by Pulse after the destroy operation is
     * complete.
     *
     * @param context the scm context that has been used for previous calls
     * @param handler handler for receipt of feedback for long-running tasks

     * @throws ScmException if there is a problem
     *
     * @see #init(ScmContext, ScmFeedbackHandler)
     */
    void destroy(ScmContext context, ScmFeedbackHandler handler) throws ScmException;

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
     * @param context  the scm context that will be available for a
     * subsequent call to the scm client based on the returned capabilities.
     * Note that in some situatations, the context is not available.  In particular,
     * when the scm is still being configured or initialised.  In that case, the
     * context will be null.
     *
     * @return a set of operations this implementation is capable of
     */
    Set<ScmCapability> getCapabilities(ScmContext context);

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
     * Returns a list of properties that should be added to the context of a
     * build.  Properties can be accessed both by the Pulse file and by commands
     * in the build.  By marking properties as add to environment it is also
     * possible to set environment variables for the build (useful when SCM
     * tools that might be called during the build are configured via the
     * environment).
     * <p/>
     * Implementations are encouraged to return as much information as
     * possible without invoking a network round trip to the SCM server.  For
     * example, configuration fields such as the location of the server may be
     * useful to the build.
     *
     * @param context defines the execution context in which the operation is
     *                being run
     * @return a list of SCM-related properties to be introduced into a build
     * @throws ScmException on error
     */
    List<ResourceProperty> getProperties(ExecutionContext context) throws ScmException;

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
    Revision checkout(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException;

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
    Revision update(ExecutionContext context, Revision revision, ScmFeedbackHandler handler) throws ScmException;

    /**
     * Checks out the specified file at the given revision.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param path     path defining the content to be retrieved.
     * @param revision the revision be checked out or null for the latest
     *                 revision (may be ignored by implementations that do
     *                 not support {@link ScmCapability#REVISIONS}).
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
     * @param context   defines the execution context in which the operation is
     *                  being run
     * @param outputDir location to store files containing the connection
     *                  details
     * @throws ScmException on error obtaining the details
     * @throws IOException  if an I/O error occurs writing the details to disk
     */
    void storeConnectionDetails(ExecutionContext context, File outputDir) throws ScmException, IOException;

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
    EOLStyle getEOLPolicy(ScmContext context) throws ScmException;

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
     * @param context  defines the scm context in which the operation is being run.
     *                  This context may be null.
     * @param path     the path to list (relative to the root of the connection,
     *                 i.e. an empty string is valid and means "list the root").
     * @param revision revision at which to browse, or null for the latest
     *                 revision (may be ignored by implementations that do not
     *                 support {@link ScmCapability#REVISIONS}).
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
     * @param scmContext   defines the scm context in which the operation is being run
     * @param context      defines the execution context in which the operation is being run
     * @param revision     the revision to be tagged
     * @param name         the name of the tag, which has an SCM-specific format
     * @param moveExisting if true and a tag of the same name already exists,
     *                     that tag will be moved to the new revision and files
     *
     * @throws ScmException on error
     */
    void tag(ScmContext scmContext, ExecutionContext context, Revision revision, String name, boolean moveExisting) throws ScmException;

    /**
     * Converts a string into a revision.  The string is input from the user,
     * and thus should be validated.  If it is invalid, an SCMException
     * should be thrown.
     * <p/>
     * Implementations may choose to support both explicit revisions and
     * symbolic ones (such as tags).  The more formats supported the greater
     * the flexibility for the user when specifying a revision to build.
     * <p/>
     * Required for {@link ScmCapability#REVISIONS}.
     *
     * @param context  defines the scm context in which the operation is being run
     * @param revision revision input string to be converted into an actual
     *                 revision
     * @return a valid revision derived from the string
     * @throws ScmException if the given revision is invalid
     */
    Revision parseRevision(ScmContext context, String revision) throws ScmException;

    /**
     * Calculates the previous revision for a revision.  The revision may have
     * come from a changelist or a file change as indicate by the isFile
     * parameter.  Implementations may return null if there is no previous
     * revision or the SCM does not have the capability.
     * <p/>
     * Note that returning symbolic revisions may not have the intended affect,
     * where possible the previous revision should be resolved to its canonical
     * form.
     * <p/>
     * Required for {@link ScmCapability#CHANGESETS}.
     *
     * @param context  defines the scm context in which the operation is being
     *                 run
     * @param revision the revision to retrieve the previous revision for
     * @param isFile   if true, the revision comes from a {@link FileChange},
     *                 otherwise it comes from a {@link Changelist}
     * @return the previous revision in the same form, or null if it does not
     *         exist or is not supported
     * @throws ScmException on any error
     */
    Revision getPreviousRevision(ScmContext context, Revision revision, boolean isFile) throws ScmException;

    /**
     * Maps from an SCM user to an email address for that user.  For SCMs that
     * have explicit user accounts with this information, providing it to Pulse
     * can be used, for example, when contacting committers.  If the user does
     * not exist or has no configured email, the implementation should return
     * null rather than throwing an exception.
     * <p/>
     * Required for {@link ScmCapability#EMAIL}.
     * 
     * @param context defines the scm context in which the operation is being
     *                run
     * @param user    name of the user to look up the email address for
     * @return the email address for the given user, or null if this user does
     *         not have an email address configured in the SCM
     * @throws ScmException on any error
     */
    String getEmailAddress(ScmContext context, String user) throws ScmException;
}
