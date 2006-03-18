package com.cinnamonbob.scm;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.Revision;

import java.io.File;
import java.util.List;

/**
 * An interface for interaction with SCM servers.
 *
 * @author jsankey
 */
public interface SCMServer
{
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
}
