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
    public CheckoutBootstrapper(Scm scm, BuildRevision revision)
    {
        super(scm, revision);
    }

    List<Change> bootstrap(File workDir)
    {
        List<Change> changes = new LinkedList<Change>();
        try
        {
            // the recipe id is not used by any of the scm servers, and is a detail they should
            // not need to be aware of.
            int recipeId = 0;
            scm.createServer().checkout(recipeId, workDir, revision.getRevision(), changes);
            return changes;
        }
        catch (SCMException e)
        {
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
