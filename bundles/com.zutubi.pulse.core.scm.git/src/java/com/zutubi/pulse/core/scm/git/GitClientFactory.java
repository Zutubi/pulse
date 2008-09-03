package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;

/**
 *
 *
 */
public class GitClientFactory implements ScmClientFactory<GitConfiguration>
{
    public ScmClient createClient(GitConfiguration config) throws ScmException
    {
        GitClient client = new GitClient();
        client.setRepository(config.getRepository());
        return client;
    }
}
