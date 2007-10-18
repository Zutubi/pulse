package com.zutubi.pulse.core.scm.cvs;

import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.cvs.config.CvsConfiguration;
import com.zutubi.util.bean.ObjectFactory;

/**
 *
 *
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
            client.setExcludedPaths(config.getFilterPaths());
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
