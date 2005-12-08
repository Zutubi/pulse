package com.cinnamonbob;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A bootstrapper that populates the working directory by checking out from one
 * or more SCMs.
 */
public class ScmBootstrapper implements Bootstrapper
{
    /**
     * Simple value type carrying details required for SCM checkout.
     */
    public class ScmCheckoutDetails
    {
        public String name;
        public SCMServer server;
        public Revision revision;
        public String path;
    }

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
        // TODO this belongs one level up
/*
        if(workDir.exists())
        {
            if(!FileSystemUtils.removeDirectory(workDir))
            {
                throw new BuildException("Unable to remove working directory '" + workDir + "'");
            }
        }
*/

        for (ScmCheckoutDetails details : checkouts)
        {
            File checkoutDir;

            if (details.path != null)
            {
                checkoutDir = new File(workDir, details.path);
            }
            else
            {
                checkoutDir = workDir;
            }

            try
            {
                // TODO this list is not needed, perhaps make it optional in SCM interface?
                details.server.checkout(checkoutDir, details.revision, new LinkedList<Change>());
            }
            catch (SCMException e)
            {
                throw new BuildException("Error checking out from SCM '" + details.name + "'", e);
            }
        }
    }
}
