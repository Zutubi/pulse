package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.git.GitClient;

/**
 * not yet implemented
 */
@SymbolicName("zutubi.gitConfigurationCheckHandler")
public class GitConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<GitConfiguration>
{
    private ScmClientFactory<ScmConfiguration> scmClientFactory;

    public void test(GitConfiguration configuration) throws ScmException
    {
        GitClient client = (GitClient) scmClientFactory.createClient(configuration);
        try
        {
            // can check the repository details by creating a local (no checkout) clone.
            // can check for the existance of the specified branch.
            // - local clone of repository
            // - list the remote branches, git remote show origin
        }
        finally
        {
            client.close();
        }
    }

    public void setScmClientFactory(ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}

