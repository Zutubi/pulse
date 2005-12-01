package com.cinnamonbob.scm.svn;

import com.cinnamonbob.model.Change;
import com.cinnamonbob.model.Changelist;
import com.cinnamonbob.model.NumericalRevision;
import com.cinnamonbob.model.Revision;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import org.tmatesoft.svn.core.ISVNWorkspace;
import org.tmatesoft.svn.core.ISVNWorkspaceListener;
import org.tmatesoft.svn.core.SVNWorkspaceManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.ws.fs.FSEntryFactory;
import org.tmatesoft.svn.core.io.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A connection to a subversion server.
 *
 * @author jsankey
 */
public class SVNServer implements SCMServer
{
    private static final int CHECKOUT_RETRIES = 1;

    private SVNRepositoryLocation location;
    private SVNRepository repository;
    private ISVNCredentialsProvider credentials;

    //=======================================================================
    // Implementation
    //=======================================================================

    /**
     * Converts a subversion exception to a generic SCMException.
     *
     * @param e the exception to convert
     * @return the converted form of the exception
     */
    private SCMException convertException(SVNException e)
    {
        e.printStackTrace();
        return new SCMException(e.getMessage(), e);
    }

    /**
     * Converts a generic revision to the subversion revision number.
     *
     * @param revision the revision to convert
     * @return the subversion revision number
     */
    private long convertRevision(Revision revision)
    {
        return ((NumericalRevision) revision).getRevisionNumber();
    }

    /**
     * Decodes a change action given a character code from subversion.
     *
     * @param type the action type as returned by the server
     * @return the corresponding Action valuie
     */
    private Change.Action decodeAction(char type)
    {
        switch (type)
        {
            case 'A':
                return Change.Action.ADD;
            case 'D':
                return Change.Action.DELETE;
            case 'M':
                return Change.Action.EDIT;
            default:
                return Change.Action.UNKNOWN;
        }
    }

    /**
     * Initialises a connection to the subversion repository.
     *
     * @param url the URL to connect to the server on
     * @throws SCMException if an error occurs connecting to the server
     */
    private void initialiseRepository(String url) throws SCMException
    {
        // Initialise SVN library
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSEntryFactory.setup();

        try
        {
            location = SVNRepositoryLocation.parseURL(url);
            repository = SVNRepositoryFactory.create(location);
            repository.setCredentialsProvider(credentials);
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }
    }

    /**
     * Helper class for identifying the files added during a checkout.
     */
    private class ChangeAccumulator implements ISVNWorkspaceListener
    {
        public List<Change> changes;

        public ChangeAccumulator(List<Change> changes)
        {
            this.changes = changes;
        }

        public void updated(String path, int contentsStatus, int propertiesStatus, long revision)
        {
            changes.add(new Change(path, Long.toString(revision), Change.Action.ADD));
        }

        public void committed(String path, int kind)
        {
        }

        public void modified(String path, int kind)
        {
        }
    }

    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url      url location of the server and module to use
     * @param username username to provide on connection
     * @param password password for the given user
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, String username, String password) throws SCMException
    {
        credentials = new SVNSimpleCredentialsProvider(username, password);
        initialiseRepository(url);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url            url location of the server and module to use
     * @param username       username to provide on connection
     * @param password       password for the given user
     * @param privateKeyFile location of the private key to provide on login
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, String username, String password, String privateKeyFile) throws SCMException
    {
        credentials = new SVNSimpleCredentialsProvider(username, password, privateKeyFile);
        initialiseRepository(url);
    }

    /**
     * Creates a new SVNServer using the given location and credentials to
     * connect to the server.
     *
     * @param url            url location of the server and module to use
     * @param username       username to provide on connection
     * @param password       password for the given user
     * @param privateKeyFile location of the private key to provide on login
     * @param passphrase     passphrase for the given private key file
     * @throws SCMException if a connection cannot be established
     */
    public SVNServer(String url, String username, String password, String privateKeyFile, String passphrase) throws SCMException
    {
        credentials = new SVNSimpleCredentialsProvider(username, password, privateKeyFile, passphrase);
        initialiseRepository(url);
    }

    //=======================================================================
    // SCMServer interface
    //=======================================================================

    /**
     * @see SCMServer#checkout(File, Revision, List<Change>)
     */
    public Revision checkout(File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        NumericalRevision svnRevision;
        ISVNWorkspace workspace;
        long revisionNumber;

        if (revision == null)
        {
            svnRevision = new NumericalRevision(ISVNWorkspace.HEAD);
        }
        else
        {
            svnRevision = (NumericalRevision) revision;
        }

        try
        {
            ChangeAccumulator accumulator = new ChangeAccumulator(changes);
            int retries = 0;

            // Workaround for an issue in javasvn: can disconnect a session
            // and then try to resuse it without connecting again.  Second
            // time around a new session is created and all is well.
            while (true)
            {
                workspace = SVNWorkspaceManager.createWorkspace("file", toDirectory.getAbsolutePath());
                workspace.setCredentials(credentials);
                workspace.addWorkspaceListener(accumulator);

                try
                {
                    revisionNumber = workspace.checkout(location, svnRevision.getRevisionNumber(), false);
                    break;
                }
                catch (SVNException e)
                {
                    if (retries++ < CHECKOUT_RETRIES)
                    {
                        continue;
                    }
                    else
                    {
                        throw convertException(e);
                    }
                }
            }
        }
        catch (SVNException e)
        {
            throw convertException(e);
        }

        return new NumericalRevision(revisionNumber);
    }

    /**
     * @see SCMServer#checkout(File, Revision, List<Change>)
     */
    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        List<Changelist>  result = new LinkedList<Changelist>();
        long fromNumber = convertRevision(from) + 1;
        long toNumber = convertRevision(to);

        if (fromNumber <= toNumber)
        {
            try
            {
                List<SVNLogEntry> logs = new LinkedList<SVNLogEntry>();

                repository.log(paths, logs, fromNumber, toNumber, true, true);
                for (SVNLogEntry entry : logs)
                {
                    NumericalRevision revision = new NumericalRevision(entry.getRevision());
                    revision.setAuthor(entry.getAuthor());
                    revision.setComment(entry.getMessage());
                    revision.setDate(entry.getDate());
                    // branch??

                    Changelist list = new Changelist(revision);
                    Map files = entry.getChangedPaths();

                    for (Object value : files.values())
                    {
                        SVNLogEntryPath entryPath = (SVNLogEntryPath) value;
                        list.addChange(new Change(entryPath.getPath(), list.getRevision().toString(), decodeAction(entryPath.getType())));
                    }

                    result.add(list);
                }
            }
            catch (SVNException e)
            {
                throw convertException(e);
            }
        }

        return result;
    }

    public boolean hasChangedSince(Revision since) throws SCMException
    {
        throw new SCMException("Operation not supported");
    }

    //=======================================================================
    // Testing use only
    //=======================================================================

    public static void main(String argv[])
    {
        try
        {
            SVNServer server = new SVNServer("svn+ssh://jason@www.anyhews.net/usr/local/svn-repo/bob/trunk", argv[0], argv[1]);
            //server.checkout(new File("/home/jsankey/svntest"), new SVNRevision(ISVNWorkspace.HEAD));
            List<Changelist> cls = server.getChanges(new NumericalRevision(47), new NumericalRevision(ISVNWorkspace.HEAD), "");

            for (Changelist l : cls)
            {
                System.out.println("Changelist:");
                System.out.println("  Revision: " + l.getRevision());
                System.out.println("  Date    : " + l.getDate());
                System.out.println("  User    : " + l.getUser());
                System.out.println("  Comment : " + l.getComment());
                System.out.println("  Files   : " + l.getRevision());

                for (Change c : l.getChanges())
                {
                    System.out.println("    " + c.getFilename() + "#" + c.getRevision() + " - " + c.getAction());
                }
            }
        }
        catch (SCMException e)
        {
            e.printStackTrace();
        }
    }
}
