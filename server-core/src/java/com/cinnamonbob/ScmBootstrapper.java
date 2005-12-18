package com.cinnamonbob;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.scm.SCMException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A bootstrapper that populates the working directory by checking out from one
 * or more SCMs.
 */
public class ScmBootstrapper implements Bootstrapper
{
    private List<ScmCheckoutDetails> checkouts;

    public ScmBootstrapper()
    {
        checkouts = new LinkedList<ScmCheckoutDetails>();
    }

    public void add(ScmCheckoutDetails details)
    {
        checkouts.add(details);
    }

    public void bootstrap(File workDir)
    {
        for (ScmCheckoutDetails details : checkouts)
        {
            Scm scm = details.scm;
            File checkoutDir;

            if (scm.getPath() != null)
            {
                checkoutDir = new File(workDir, scm.getPath());
            }
            else
            {
                checkoutDir = workDir;
            }

            try
            {
                // TODO this list is not needed, perhaps make it optional in SCM interface?
                details.scm.createServer().checkout(checkoutDir, details.revision, new LinkedList<Change>());
            }
            catch (SCMException e)
            {
                throw new BuildException("Error checking out from SCM '" + scm.getName() + "'", e);
            }
        }
    }
}
