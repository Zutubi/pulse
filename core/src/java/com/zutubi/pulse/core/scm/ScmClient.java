package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    Set<ScmCapability> getCapabilities();

    /**
     * Returns a string that uniquely identifies the server itself.  This may
     * include the server address and repository root, for example.  All
     * SCMServer objects talking to the same SCM should return the same id.
     *
     * @return a unique id for the SCM server
     * @throws ScmException on error
     */
    String getUid() throws ScmException;

    /**
     * Returns a summarised form of the location of the source this SCM has
     * been configured to check out.  For example, an subversion URL.
     *
     * Required for all implementations.
     *
     * @return a summarised form of the source location, fit for human
     * consumption
     * @throws ScmException on error
     */
    String getLocation() throws ScmException;

    /**
     * Checks out a new working copy to the specified directory.
     *
     * Required for all implementations.
     *
     * @param context
     * @param handler     if not null, receives notifications of events during the
     */
    Revision checkout(ScmContext context, ScmEventHandler handler) throws ScmException;

    /**
     * Update the working directory to the specified revision.
     *
     * @param context
     * @param handler if not null, receives notifications of events during the
     */
    void update(ScmContext context, ScmEventHandler handler) throws ScmException;

    /**
     * Checks out the specified file at the given revision.
     *
     * @param path
     * @param revision the revision be checked out @return an input stream that will return the contents of the requested file
     * @throws ScmException on error
     */
    InputStream retrieve(String path, Revision revision) throws ScmException;

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
     * @throws ScmException on error
     */
    List<ResourceProperty> getProperties(String id, File dir) throws ScmException;

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
     * @throws ScmException on error obtaining the details
     * @throws IOException if an I/O error occurs writing the details to disk
     */
    void storeConnectionDetails(File outputDir) throws ScmException, IOException;

    /**
     * Returns the policy for line endings enforced at a client level, if any.
     *
     * Required for all implementations.
     *
     * @return the EOL policy, which will be EOLStyle.BINARY if no policy is
     *         in effect
     * @throws ScmException on error
     */
    FileStatus.EOLStyle getEOLPolicy() throws ScmException;

    /**
     * Returns the latest repository revision.
     *
     * @return the latest revision in the repository
     * @throws ScmException on error
     */
    Revision getLatestRevision() throws ScmException;

    /**
     * Returns a list of revisions occuring between the given revisions.
     * The from revision itself it NOT included in the result.
     *
     * @param from the revision before the first revision to return
     * @param to
     * @return a list of revisions for all changes since from
     * @throws ScmException if an error occurs talking to the server
     */
    List<Revision> getRevisions(Revision from, Revision to) throws ScmException;

    /**
     * Returns a list of changelists occuring in between the given revisions.
     * The changelist that created the from revision itself is NOT included in
     * the model.
     *
     * Required for {@link ScmCapability#CHANGESET}.
     *
     * @param from  the revision before the first changelist to include in the model
     * @param to    the last revision to include in the model
     * @return a list of changelists that occured between the two revisions
     * @throws ScmException if an error occurs talking to the server
     */
    List<Changelist> getChanges(Revision from, Revision to) throws ScmException;

    /**
     * Returns a list of all files/directories in the given path (which
     * should specify a directory).  This function is NOT recursive, i.e.
     * only direct descendents should be listed.
     *
     * Required for {@link ScmCapability#BROWSE}.
     *
     * @param path the path to list (relative to the root of the connection,
     *             i.e. an empty string is valid and means "list the root").
     * @return a list of files and directories contained within the given
     *         path
     * @throws ScmException on error
     */
    List<ScmFile> browse(String path) throws ScmException;

    /**
     * Applies a tag to the given revision of all files in the server's view .
     *
     * Required for {@link ScmCapability#TAG}.
     *
     * @param revision     the revision to be tagged
     * @param name         the name of the tag, which has an SCM-specific format
     * @param moveExisting if true and a tag of the same name already exists,
     *                     that tag will be moved to the new revision and files
     * @throws ScmException on error
     */
    void tag(Revision revision, String name, boolean moveExisting) throws ScmException;

    /**
     * Converts a string into a revision.  The string is input from the user,
     * and thus should be validated.  If it is invalid, an SCMException
     * should be thrown.
     *
     * @param revision revision input string to be converted into an actual
     *                 revision
     * @return a valid revision derived from the string
     * @throws ScmException if the given revision is invalid
     */
    Revision getRevision(String revision) throws ScmException;
}
