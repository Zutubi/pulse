package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The Cvs Server provides all interactions with a cvs repository.
 */
public class CvsServer implements SCMServer
{
    private String cvsRoot;
    private String cvsModule;

    private List<Changelist> EMPTY_LIST = Collections.unmodifiableList(new LinkedList<Changelist>());

    private static final Logger LOG = Logger.getLogger(CvsServer.class);

    public CvsServer(String root, String module)
    {
        this.cvsRoot = root;
        this.cvsModule = module;
    }

    public String getLocation()
    {
        return cvsRoot + " [" + cvsModule + "]";
    }

    public Revision checkout(long id, File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        try
        {
            Revision checkedOutRevision = checkout(toDirectory, revision);
            if (changes != null)
            {
                for (Changelist changelist : getChanges(revision, checkedOutRevision))
                {
                    changes.addAll(changelist.getChanges());
                }
            }

            return checkedOutRevision;
        }
        catch (IOException e)
        {
            throw new SCMException(e);
        }
    }

    public String checkout(Revision revision, String file) throws SCMException
    {
        if (revision == null)
        {
            throw new IllegalArgumentException("can not checkout a null revision of file '" + file + "'.");
        }
        if (!TextUtils.stringSet(file))
        {
            throw new IllegalArgumentException("You need to specify a file to checkout.");
        }

        File tmpDir = null;
        try
        {
            tmpDir = FileSystemUtils.createTempDirectory("CvsServer", "checkout");
            String branch = revision.getBranch();
            Date date = revision.getDate();

            internalCheckout(tmpDir, branch, date, file, null);

            // read checked out file.
            InputStream in = null;
            try
            {
                File checkedOutFile = new File(tmpDir, file);
                if (!checkedOutFile.exists())
                {
                    throw new SCMException("Unable to checkout file '" + file + "' from cvs[" + cvsRoot + "].");
                }
                in = new FileInputStream(checkedOutFile);
                return IOUtils.inputStreamToString(in);
            }
            finally
            {
                IOUtils.close(in);
            }
        }
        catch (IOException e)
        {
            LOG.severe(e);
            throw new SCMException("Unable to retrieve requested file: " + file, e);
        }
        finally
        {
            FileSystemUtils.removeDirectory(tmpDir);
        }
    }

    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        if (from == null)
        {
            return EMPTY_LIST;
        }

        Date since = from.getDate();

        CvsClient client = new CvsClient(cvsRoot);
        List<Changelist> changelists = client.getChangeLists(since, cvsModule);

        // filter out any changelists that fall outside the date range.
        if (to != null)
        {
            CvsRevision toRevision = (CvsRevision) to;
            Iterator<Changelist> i = changelists.iterator();
            while (i.hasNext())
            {
                Changelist cl = i.next();
                if (toRevision.getDate().compareTo(cl.getDate()) > 0)
                {
                    i.remove();
                }
            }
        }
        return changelists;
    }

    /**
     * This method checks to see if there have been any changes to the scm system since the
     * specified revision.
     *
     * @param since
     * @return true if a change has been detected, false otherwise.
     * @throws SCMException
     */
    public boolean hasChangedSince(Revision since) throws SCMException
    {
        if (since.getDate() == null)
        {
            throw new IllegalArgumentException("since revision date can not be null.");
        }

        CvsClient client = new CvsClient(cvsRoot);
        if (TextUtils.stringSet(since.getBranch()))
        {
            client.setTag(since.getBranch());
        }
        return client.hasChangedSince(since.getDate(), cvsModule);
    }

    /**
     * Checkout head of the specified branch to the specified directory.
     *
     * @param checkoutDir (required) if this directory does not exist, an attempt will be
     *                    made to create it.
     * @param branch      (optional)
     * @return
     * @throws SCMException
     */
    public Revision checkout(File checkoutDir, String branch) throws SCMException, IOException
    {
        // cvs is not atomic, so take the current time and restrict the checkout to 'now'
        // to prevent problems with people checking in during the checkout.
        Date now = new Date(System.currentTimeMillis());

        internalCheckout(checkoutDir, branch, now, cvsModule, null);
        return new CvsRevision(null, branch, null, now);
    }

    /**
     * @param checkoutDir (required)
     * @param revision    (required)
     * @return
     * @throws SCMException
     * @throws IOException
     */
    public Revision checkout(File checkoutDir, Revision revision) throws SCMException, IOException
    {
        if (revision == null)
        {
            throw new IllegalArgumentException("Revision is a required argument.");
        }
        if (!(revision instanceof CvsRevision))
        {
            throw new IllegalArgumentException("Unsupported revision type: " + revision.getClass() + ".");
        }

        internalCheckout(checkoutDir, revision.getBranch(), revision.getDate(), cvsModule, null);
        return revision;
    }

    /**
     * Internal checkout method. This is where all the action is.
     *
     * @param checkoutDir (required)
     * @param branch      (optional)
     * @param date        (optional)
     * @param tag         (not supported)
     */
    private void internalCheckout(File checkoutDir, String branch, Date date, String serverPath, String tag) throws IOException, SCMException
    {
        if (checkoutDir == null)
        {
            throw new IllegalArgumentException("checkoutDir is a required paramenter.");
        }
        if (!checkoutDir.exists() && !checkoutDir.mkdirs())
        {
            throw new IOException("Failed to create checkout directory: " + checkoutDir);
        }
        if (!checkoutDir.isDirectory())
        {
            throw new IllegalArgumentException("checkoutDir must refer to a directory.");
        }

        CvsClient client = new CvsClient(cvsRoot);
        client.setLocalPath(checkoutDir);
        if (TextUtils.stringSet(branch))
        {
            client.setTag(branch);
        }
        client.checkout(serverPath, date);
    }

    public Revision getLatestRevision() throws SCMException
    {
        // this assumes that we are not dealing with a branch. If that were the case, then
        // we would need to include the branch details in the revision.
        // Question: do the other scms care about branches? SVN does not, what about P4?

        // cvs is not atomic, so take the current time and restrict the checkout to 'now'
        // to prevent problems with people checking in during the checkout.

        Date now = new Date(System.currentTimeMillis()); // this needs to be GMT 00.
        return new CvsRevision(null, null, null, now);
    }
}
