package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.RemoteFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * An interface for interaction with SCM servers.
 *
 * @author jsankey
 */
public interface SCMServer
{
    /**
     * Returns information about the server, as name-value pairs.
     *
     * @return a set of name-value pairs giving information about the server
     */
    Map<String, String> getServerInfo() throws SCMException;

    /**
     * Returns a string that uniquely identifies the server itself.  This may
     * include the server address and repository root, for example.  All
     * SCMServer objects talking to the same SCM should return the same id.
     *
     * @return a unique id for the SCM server
     */
    String getUid() throws SCMException;

    /**
     * Returns a summarised form of the location of the source this SCM has
     * been configured to check out.  For example, an subversion URL.
     *
     * @return a summarised form of the source location, fit for human consumption
     */
    String getLocation();

    /**
     * Run a check on the connection to the scm server.
     *
     * @throws SCMException if there are any problems.
     */
    void testConnection() throws SCMException;

    /**
     * Checks out a new working copy to the specified directory.
     *
     * @param id          a unique identifier for this checkout
     * @param toDirectory root directory to check the copy out to
     * @param revision    the revision to check out, or null for most recent (HEAD)
     * @param changes     if not null, receives a list of change objects
     *                    indicating the files that were checked out (the
     *                    action will be ADD)
     * @return the revision actually checked out
     * @throws SCMException if an error occurs communicating with the server
     */
    Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException;

    /**
     * Checkout the specified file.
     *
     * @param id       a unique identifier for this checkout
     * @param revision the revision be checked out
     * @param file     the path of the file relative to the configured scms checkout path
     * @return the contents of the requested file
     * @throws SCMException
     */
    String checkout(long id, Revision revision, String file) throws SCMException;

    /**
     * Returns a list of changelists occuring in between the given revisions.
     * The changelist that created the from revision itself is NOT included in
     * the model.
     *
     * @param from  the revision before the first changelist to include in the model
     * @param to    the last revision to include in the model
     * @param paths an array of paths to restrict the query to, relative to the root
     *              of this connection to the server (a path of "" will include all
     *              changes)
     * @return a list of changelists that occured between the two revisions
     * @throws SCMException if an error occurs talking to the server
     */
    List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException;

    /**
     * Returns a boolean indicated whether or not a change has occured since the specified revision.
     *
     * @param since
     * @return true if there has been a change
     * @throws SCMException
     */
    boolean hasChangedSince(Revision since) throws SCMException;

    /**
     * Returns the latest repository revision or null if it can not be determined.
     *
     * @return
     * @throws SCMException
     */
    Revision getLatestRevision() throws SCMException;

    /**
     * Returns details of a file or directory in the repository.
     *
     * @param path path to the file, relative to this connection's root
     * @return the file details
     */
    RemoteFile getFile(String path) throws SCMException;

    /**
     * Returns a list of all files/directories in the given path (which
     * should specify a directory).  This function is NOT recursive.
     *
     * @param path the path to list (relative to the root of the connection,
     *             i.e. an empty string is valid and means "list the root").
     * @return a list of files and directories contained within the given
     *         path
     */
    List<RemoteFile> getListing(String path) throws SCMException;

    /**
     * Update the working directory to the specified revision.
     *
     * @param workDir contains a local copy (checkout) of the module.
     * @param rev to to which the local copy will be updated.
     *
     * @throws SCMException
     */
    void update(File workDir, Revision rev) throws SCMException;

    /**
     * Allows the scm server to indicate whether or not it supports the update
     * operation.
     *
     * @return true if update is supported, false otherwise.
     */
    boolean supportsUpdate();
}
