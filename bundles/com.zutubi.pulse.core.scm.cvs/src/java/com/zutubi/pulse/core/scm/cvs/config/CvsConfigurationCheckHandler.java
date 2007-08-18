package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsClient;

/**
 *
 *
 */
@SymbolicName("zutubi.cvsConfigurationCheckHandler")
public class CvsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<CvsConfiguration>
{
    private ScmClientFactory<Configuration> scmClientFactory;

    public void test(CvsConfiguration configuration) throws ScmException
    {
        CvsClient client = (CvsClient) scmClientFactory.createClient(configuration);
        client.testConnection();
    }

    public void setScmClientFactory(ScmClientFactory<Configuration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}
