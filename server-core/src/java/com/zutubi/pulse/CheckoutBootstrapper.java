package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    private boolean persist;

    public CheckoutBootstrapper(String project, String spec, Scm scm, BuildRevision revision, boolean persist)
    {
        super(project, spec, scm, revision);
        this.persist = persist;
    }

    List<Change> bootstrap(File workDir)
    {
        List<Change> changes = new LinkedList<Change>();
        try
        {
            String id = null;
            if(persist)
            {
                id = getId();
            }

            scm.createServer().checkout(id, workDir, revision.getRevision(), changes);
            return changes;
        }
        catch (SCMException e)
        {
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
