package com.cinnamonbob.scm.cvs;

import com.cinnamonbob.scm.SCMServer;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.model.Revision;
import com.cinnamonbob.model.Change;
import com.cinnamonbob.model.Changelist;
import com.cinnamonbob.model.CvsRevision;

import java.io.File;
import java.util.*;

/**
 * The Cvs Server provides all interactions with a cvs repository.
 */
public class CvsServer implements SCMServer
{
    private String cvsRoot;
    private String cvsModule;

    private List<Changelist> EMPTY_LIST = Collections.unmodifiableList(new LinkedList<Changelist>());

    public CvsServer(String root, String module)
    {
        this.cvsRoot = root;
        this.cvsModule = module;
    }

    public Revision checkout(File toDirectory, Revision revision, List<Change> changes) throws SCMException
    {
        // can not checkout a revision since cvs does not support repository wide revisions.
        // can however checkout a tag / branch...?

        CvsRevision cvsRevision = (CvsRevision) revision;

        // what about the changes? what do these represent?
        CvsClient client = new CvsClient(cvsRoot);
        client.setLocalPath(toDirectory);
        client.checkout(cvsModule);

        // return the revision object representing the latest change
        // that was included in the checkout. we could work out an approximation, but
        // until this is better understood, just return the revision object passed in.

        return revision;
    }

    public List<Changelist> getChanges(Revision from, Revision to, String ...paths) throws SCMException
    {
        if (from == null)
        {
            return EMPTY_LIST;
        }

        CvsRevision fromRevision = (CvsRevision) from;
        Date since = fromRevision.getDate();

        CvsClient client = new CvsClient(cvsRoot);
        List<Changelist> changelists = client.getChangeLists(since);

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
}
