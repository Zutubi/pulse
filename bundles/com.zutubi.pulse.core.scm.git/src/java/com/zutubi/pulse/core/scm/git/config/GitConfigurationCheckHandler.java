package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.tove.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.core.scm.git.GitClient;
import com.zutubi.pulse.core.scm.ScmClientUtils;

/**
 * not yet implemented
 */
@Wire
@SymbolicName("zutubi.gitConfigurationCheckHandler")
public class GitConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<GitConfiguration>
{
    private ScmClientFactory<ScmConfiguration> scmClientFactory;

    public void test(GitConfiguration configuration) throws ScmException
    {
        GitClient client = null;
        try
        {
            client = (GitClient) scmClientFactory.createClient(configuration);
            // can check the repository details by creating a local (no checkout) clone.
            // can check for the existance of the specified branch.
            // - local clone of repository
            // - list the remote branches, git remote show origin
        }
        finally
        {
            ScmClientUtils.close(client);
        }
    }

    public void setScmClientFactory(ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}

