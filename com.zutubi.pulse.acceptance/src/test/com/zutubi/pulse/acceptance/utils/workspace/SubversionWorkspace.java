package com.zutubi.pulse.acceptance.utils.workspace;

import com.zutubi.util.io.FileSystemUtils;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Utility class that provides assistance when working with an svn workspace.
 */
public class SubversionWorkspace implements Closeable
{
    static
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
    }

    private SVNClientManager clientManager;
    private File workingDir;

    /**
     * Create a new instance.
     *
     * @param workingDir    the working directory in which the svn operations will take place.
     * @param user          the username used to authenticate the svn operations.
     * @param pass          the password used to authenticate the svn operations.
     */
    public SubversionWorkspace(File workingDir, String user, String pass)
    {
        SVNRepositoryFactoryImpl.setup();

        BasicAuthenticationManager authenticationManager = new BasicAuthenticationManager(user, pass);
        this.clientManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), authenticationManager);
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
     * Copies one subversion URL to another, server side.
     *
     * @param comment the commit comment for the copy change
     * @param fromUrl source URL
     * @param toUrl   destination URL
     * @throws SVNException on error
     */
    public void doCopy(String comment, String fromUrl, String toUrl) throws SVNException
    {
        SVNCopyClient copyClient = clientManager.getCopyClient();
        SVNCopySource[] copySources = {new SVNCopySource(SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNURL.parseURIDecoded(fromUrl))};
        copyClient.doCopy(copySources, SVNURL.parseURIDecoded(toUrl), false, true, true, comment, null);
    }

    /**
     * Conveience method to edit and commit a file.
     * 
     * @param filename   path of the file, relative to the workspace root, to edit
     * @param comment    commit comment
     * @param newContent new content of the edited file, in its entirety (note that this must differ
     *                   from the current content or no change will be committed)
     * @return the committed revision
     * @throws IOException on an error writing to the file
     * @throws SVNException on any error interacting with svn
     */
    public String editAndCommitFile(String filename, String comment, String newContent) throws IOException, SVNException
    {
        File file = new File(workingDir, filename);
        if (!file.exists())
        {
            throw new AssertionError("Attempt to edit file that does not exist (" + filename + ")");
        }
        FileSystemUtils.createFile(file, newContent);
        return doCommit(comment, file);
    }

    /**
     * Cleanup resources held by this instance.  This includes cleaning up the working
     * directory.
     */
    public void close() throws IOException
    {
        clientManager.dispose();
        FileSystemUtils.rmdir(workingDir);
    }
}
