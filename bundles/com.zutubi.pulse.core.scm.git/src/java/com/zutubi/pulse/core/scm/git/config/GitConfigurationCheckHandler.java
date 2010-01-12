package com.zutubi.pulse.core.scm.git.config;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.git.GitClient;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

/**
 * Tests connections to git repositories.
 */
@Wire
@SymbolicName("zutubi.gitConfigurationCheckHandler")
public class GitConfigurationCheckHandler extends AbstractConfigurationCheckHandler<GitConfiguration>
{
    private ScmClientFactory<? super GitConfiguration> scmClientFactory;

    public void test(GitConfiguration configuration) throws ScmException
    {
        GitClient client = null;
        try
        {
            client = (GitClient) scmClientFactory.createClient(configuration);
            client.testConnection();
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }
    }

    public void setScmClientFactory(ScmClientFactory<? super GitConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}

