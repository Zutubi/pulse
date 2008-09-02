package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;

/**
 *
 *
 */
public class GitClientFactory implements ScmClientFactory
{
    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        return new GitClient();
    }
}
