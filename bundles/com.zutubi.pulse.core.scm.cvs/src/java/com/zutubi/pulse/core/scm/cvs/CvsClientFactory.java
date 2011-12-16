package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.cvs.config.CvsConfiguration;
import com.zutubi.util.bean.ObjectFactory;

/**
 * Scm client factory implementation that uses a CvsConfiguration to create a cvs client.
 */
public class CvsClientFactory implements ScmClientFactory<CvsConfiguration>
{
    private ObjectFactory objectFactory;

    private static final Class[] CONSTRUCTOR_ARGS = new Class[]{String.class, String.class, String.class, String.class};

    public ScmClient createClient(CvsConfiguration config) throws ScmException
    {
        try
        {
            CvsClient client = objectFactory.buildBean(CvsClient.class, CONSTRUCTOR_ARGS,
                    new Object[]{config.getRoot(),
                            config.getModule(),
                            config.getPassword(),
                            config.getBranch()
                    }
            );
            client.setFilterPaths(config.getIncludedPaths(), config.getExcludedPaths());
            return client;
        }
        catch (Exception e)
        {
            throw new ScmException(e);
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
