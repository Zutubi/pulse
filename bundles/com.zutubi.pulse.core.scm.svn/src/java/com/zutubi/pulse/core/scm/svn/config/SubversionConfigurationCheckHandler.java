package com.zutubi.pulse.core.scm.svn.config;

import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.svn.SubversionClient;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

/**
 */
@Wire
@SymbolicName("zutubi.subversionConfigurationCheckHandler")
public class SubversionConfigurationCheckHandler extends AbstractConfigurationCheckHandler<SubversionConfiguration>
{
    private ScmClientFactory<? super SubversionConfiguration> scmClientFactory;

    public void test(SubversionConfiguration configuration) throws ScmException
    {
        SubversionClient client = null;
        try
        {
            client = (SubversionClient) scmClientFactory.createClient(configuration);
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

    public void setScmClientFactory(ScmClientFactory<? super SubversionConfiguration> scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
