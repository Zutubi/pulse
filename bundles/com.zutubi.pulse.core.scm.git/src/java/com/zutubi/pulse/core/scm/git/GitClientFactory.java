package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.ScmClientFactory} to handle the
 * creation of the Git ScmClient.
 *
 * @see com.zutubi.pulse.core.scm.git.GitClient
 */
public class GitClientFactory implements ScmClientFactory<GitConfiguration>
{
    public ScmClient createClient(GitConfiguration config) throws ScmException
    {
        GitClient client = new GitClient();
        client.setRepository(config.getRepository());
        client.setBranch(config.getBranch());
        
        return client;
    }
}
