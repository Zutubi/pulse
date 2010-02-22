package com.zutubi.pulse.acceptance.utils;

import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;

import com.zutubi.util.FileSystemUtils;

/**
 * Utility class that provides assistance when working with an svn workspace.
 */
public class SvnWorkspace
{
    private SVNClientManager clientManager;
    private File workingDir;

    /**
     * Create a new instance.
     *
     * @param clientManager     the configured client manager that provides access to the svn operations.
     * @param workingDir        the working directory in which the svn operations will take place.
     */
    public SvnWorkspace(SVNClientManager clientManager, File workingDir)
    {
        this.clientManager = clientManager;
        this.workingDir = workingDir;
    }

    /**
     * Checkout the content from the specified url into the working directory.
     *
     * @param url   the svn url to checkout.
     *
     * @throws SVNException on error.
     */
    public void doCheckout(String url) throws SVNException
    {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(SVNURL.parseURIDecoded(url), workingDir, SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, false);
    }

    /**
     * Add the specified files to svn.
     *
     * @param files the files to be added.
     *
     * @throws SVNException on error.
     */
    public void doAdd(File... files) throws SVNException
    {
        SVNWCClient client = clientManager.getWCClient();
        client.doAdd(files, true, false, false, SVNDepth.EMPTY, false, false, false);
    }

    /**
     * Commit the changes in the specified files to svn.
     *
     * @param comment   the comment associated with the change.
     * @param files     the files to be committed.
     * @return the repository revision number associated with this commit.
     *
     * @throws SVNException on error.
     */
    public String doCommit(String comment, File... files) throws SVNException
    {
        SVNCommitClient commitClient = clientManager.getCommitClient();
        SVNCommitInfo info = commitClient.doCommit(files, true, comment, null, null, false, false, SVNDepth.EMPTY);
        return String.valueOf(info.getNewRevision());
    }

    /**
     * Cleanup resources held by this instance.  This includes cleaning up the working
     * directory.
     */
    public void dispose()
    {
        clientManager.dispose();
        FileSystemUtils.rmdir(workingDir);
    }
}
