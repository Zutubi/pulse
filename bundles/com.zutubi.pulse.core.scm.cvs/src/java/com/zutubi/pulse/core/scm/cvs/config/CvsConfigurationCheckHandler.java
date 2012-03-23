package com.zutubi.pulse.core.scm.cvs.config;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.CvsClient;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

@SymbolicName("zutubi.cvsConfigurationCheckHandler")
@Wire
public class CvsConfigurationCheckHandler extends AbstractConfigurationCheckHandler<CvsConfiguration>
{
    private ScmClientFactory<? super CvsConfiguration> scmClientFactory;

    public void test(CvsConfiguration configuration) throws ScmException
    {
        CvsClient client = null;
        try
        {
            client = (CvsClient) scmClientFactory.createClient(configuration);
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

    public void setScmClientFactory(ScmClientFactory<? super CvsConfiguration> scmClientManager)
    {
        this.scmClientFactory = scmClientManager;
    }
}
