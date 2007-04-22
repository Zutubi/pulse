package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An interface for interaction with SCM servers.
 */
public interface ScmClient
{
    /**
     * Returns the functionality that this implementation supports, as a set
     * of capabilities.  Each method's documentation indicates the capability
     * that it helps fulfill.  Before calling a method, ensure that it is
     * supported by checking the associated capability.
     *
     * @return a set of operations this implementation is capable of
     */
    Set<SCMCapability> getCapabilities();

    /**
     * Returns information about the server, as name-value pairs.
     *
     * Required for all implementations.
     *
     * @return a set of name-value pairs giving information about the server
     * @throws SCMException on error
     */
    Map<String, String> getServerInfo() throws SCMException;

    /**
     * Returns a summarised form of the location of the source this SCM has
     * been configured to check out.  For example, an subversion URL.
     *
     * Required for all implementations.
     *
     * @return a summarised form of the source location, fit for human
     * consumption
     * @throws SCMException on error
     */
    String getLocation() throws SCMException;

    /**
     * Checks out a new working copy to the specified directory.
     *
     * Required for all implementations.
     *
     * @param id          an identifier for this checkout used to identify related
     *                    checkout/update operations.  May be null to indicate no
     *                    relationship.
     * @param toDirectory root directory to check out to
     * @param revision    the revision to check out, or null for most recent
     *                    or when revisions are not supported
     * @param handler     if not null, receives notifications of events during the
     *                    checkout operation
     * @return the revision actually checked out
     * @throws SCMException on error
     */
    Revision checkout(String id, File toDirectory, Revision revision, SCMCheckoutEventHandler handler) throws SCMException;

    /**
     * Returns variables that should be added to the build environment for
     * this SCM.  This is useful if commands run in the build may need access
     * to the information, but may be unnecessary for SCMs where the
     * information is already present in the working copy (e.g. Subversion).
     * In this latter case an empty map may be returned.
     *
     * Required for all implementations.
     *
     * @param id  identifier linking this request with a previous checkout
     *            operation, or null if no such relationship exists.
     * @param dir directory containing the working copy for the build
     * @return properties to introduce into the build environment
     * @throws SCMException on error
     */
    Map<String, String> getProperties(String id, File dir) throws SCMException;

    /**
     * Stores details about the connection to the server to the given
     * location on disk.  These details should capture all information
     * needed to reproduce the same build, and may also include
     * miscellaneous information the is useful to the user.  If there is no
     * additional information to store, this operation may be a no-op.
     *
     * Required for all implementations.
     *
     * @param outputDir location to store files containing the connection
     *                  details
     * @throws SCMException on error obtaining the details
     * @throws IOException if an I/O error occurs writing the details to disk
     */
    void storeConnectionDetails(File outputDir) throws SCMException, IOException;

    /**
     * Returns the policy for line endings enforced at a client level, if any.
     *
     * Required for all implementations.
     *
     * @return the EOL policy, which will be EOLStyle.BINARY if no policy is
     *         in effect
     * @throws SCMException on error
     */
    FileStatus.EOLStyle getEOLPolicy() throws SCMException;

    /**
     * Returns the latest repository revision.
     *
     * Required for {@link SCMCapability#LATEST_REVISION}.
     *
     * @return the latest revision in the repository
     * @throws SCMException on error
     */
    Revision getLatestRevision() throws SCMException;

    /**
     * Returns a string that uniquely identifies the server itself.  This may
     * include the server address and repository root, for example.  All
     * SCMServer objects talking to the same SCM should return the same id.
     *
     * Required for {@link SCMCapability#LIST_CHANGES}.
     *
     * @return a unique id for the SCM server
     * @throws SCMException on error
     */
    String getUid() throws SCMException;

    /**
     * Run a check on the connection to the SCM server.  If there is a
     * problem contacting the server, an exception is thrown.
     *
     * Required for {@link SCMCapability#TEST_CONNECTION}.
     * 
     * @throws SCMException if there are any problems connecting.
     */
    void testConnection() throws SCMException;

    /**
     * Checks out the specified file at the given revision.
     *
     * Required for {@link SCMCapability#CHECKOUT_FILE}.
     *
     * @param revision the revision be checked out
     * @param file     the path of the file relative to the configured scms checkout path
     * @return an input stream that will return the contents of the requested file
     * @throws SCMException on error
     */
    InputStream checkout(Revision revision, String file) throws SCMException;

    /**
     * Returns a list of changelists occuring in between the given revisions.
     * The changelist that created the from revision itself is NOT included in
     * the model.
     *
     * Required for {@link SCMCapability#LIST_CHANGES}.
     *
     * @param from  the revision before the first changelist to include in the model
     * @param to    the last revision to include in the model
     * @return a list of changelists that occured between the two revisions
     * @throws SCMException if an error occurs talking to the server
     */
    List<Changelist> getChanges(Revision from, Revision to) throws SCMException;

    /**
     * Returns a list of revisions occuring between the given revision and now.
     * The from revision itself it NOT included in the result.
     *
     * Required for {@link SCMCapability#POLL}.
     *
     * @param from the revision before the first revision to return
     * @return a list of revisions for all changes since from
     * @throws SCMException if an error occurs talking to the server
     */
    List<Revision> getRevisionsSince(Revision from) throws SCMException;

    /**
     * Returns true iff a change has occured since the specified revision.
     *
     * Required for {@link SCMCapability#POLL}.
     *
     * @param since the revision to check from (changes in this revision are
     *              not included)
     * @return true iff there has been a change since the revision
     * @throws SCMException on error
     */
    boolean hasChangedSince(Revision since) throws SCMException;

    /**
     * Returns details of a file or directory in the repository.
     *
     * Required for {@link SCMCapability#BROWSE}.
     *
     * @param path path to the file, relative to this connection's root
     * @return the file details
     * @throws SCMException on error
     */
    SCMFile getFile(String path) throws SCMException;

    /**
     * Returns a list of all files/directories in the given path (which
     * should specify a directory).  This function is NOT recursive, i.e.
     * only direct descendents should be listed.
     *
     * Required for {@link SCMCapability#BROWSE}.
     *
     * @param path the path to list (relative to the root of the connection,
     *             i.e. an empty string is valid and means "list the root").
     * @return a list of files and directories contained within the given
     *         path
     * @throws SCMException on error
     */
    List<SCMFile> getListing(String path) throws SCMException;

    /**
     * Update the working directory to the specified revision.
     *
     * Required for {@link SCMCapability#UPDATE}.
     *
     * @param id      an identifier for this update used to identify related
     *                checkout/update operations.  May be null to indicate no
     *                relationship.
     * @param workDir contains a local copy (checkout) of the module.
     * @param rev     revision to which the local copy will be updated.
     * @param handler if not null, receives notifications of events during the
     *                update operation
     * @throws SCMException on error
     */
    void update(String id, File workDir, Revision rev, SCMCheckoutEventHandler handler) throws SCMException;

    /**
     * Applies a tag to the given revision of all files in the server's view .
     *
     * Required for {@link SCMCapability#TAG}.
     *
     * @param revision     the revision to be tagged
     * @param name         the name of the tag, which has an SCM-specific format
     * @param moveExisting if true and a tag of the same name already exists,
     *                     that tag will be moved to the new revision and files
     * @throws SCMException on error
     */
    void tag(Revision revision, String name, boolean moveExisting) throws SCMException;

    /**
     * Converts a string into a revision.  The string is input from the user,
     * and thus should be validated.  If it is invalid, an SCMException
     * should be thrown.
     *
     * Required for {@link SCMCapability#CHECKOUT_AT_REVISION}.
     * 
     * @param revision revision input string to be converted into an actual
     *                 revision
     * @return a valid revision derived from the string
     * @throws SCMException if the given revision is invalid
     */
    Revision getRevision(String revision) throws SCMException;
}
