package com.zutubi.pulse.scm.cvs.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.ScmClientFactory;
import com.zutubi.pulse.scm.cvs.config.CvsConfiguration;
import com.zutubi.pulse.scm.cvs.CvsClient;

/**
 *
 *
 */
@SymbolicName("zutubi.cvsConfigurationCheckHandler")
public class CvsConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<CvsConfiguration>
{
    private ScmClientFactory scmClientFactory;

    public void test(CvsConfiguration configuration) throws ScmException
    {
        CvsClient client = (CvsClient) scmClientFactory.createClient(configuration);
        client.testConnection();
    }

    public void setScmClientFactory(ScmClientFactory scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}
