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
                // in future, look into running an rlog command here.
                throw new RuntimeException("gathering the revisions of all the files checked " +
                        "out is not yet implemented.");
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
            String tag = revision.getBranch();
            Date date = revision.getDate();

            internalCheckout(tmpDir, tag, date, file);

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
            if (!FileSystemUtils.removeDirectory(tmpDir))
            {
                LOG.severe("failed to remove temporary directory " + tmpDir);
            }
        }
    }

    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        // assert that the branch for both revisions is the same. We do not support retrieving
        // differences across multiple branches/revisions. For practical reasons, we do not need to...

        CvsClient client = new CvsClient(cvsRoot);

        // paths...??

        return client.getChangeLists(cvsModule, from.getBranch(), from.getDate(), to.getDate());
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
        return client.hasChangedSince(cvsModule, since.getBranch(), since.getDate());
    }

    /**
     * Checkout the latest of the specified tag to the specified directory.
     *
     * @param checkoutDir (required) if this directory does not exist, an attempt will be
     *                    made to create it.
     * @param tag      (optional)
     * @return
     * @throws SCMException
     */
    public Revision checkout(File checkoutDir, String tag) throws SCMException, IOException
    {
        // cvs is not atomic, so take the current time and restrict the checkout to 'now'
        // to prevent problems with people checking in during the checkout.
        Date now = new Date(System.currentTimeMillis());

        internalCheckout(checkoutDir, tag, now, cvsModule);
        return new CvsRevision(null, tag, null, now);
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
        CvsRevision cvsRevision = (CvsRevision) revision;
        Date checkoutDate = cvsRevision.getDate();
        if (checkoutDate == null)
        {
            checkoutDate = new Date(System.currentTimeMillis());
        }
        internalCheckout(checkoutDir, cvsRevision.getBranch(), checkoutDate, cvsModule);

        return new CvsRevision(null, revision.getBranch(), null, checkoutDate);
    }

    /**
     * Internal checkout method. This is where all the action is.
     *
     * @param checkoutDir (required)
     * @param revision      (optional)
     * @param date        (optional)
     */
    private void internalCheckout(File checkoutDir, String revision, Date date, String serverPath) throws IOException, SCMException
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
        client.checkout(serverPath, revision, date);
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
