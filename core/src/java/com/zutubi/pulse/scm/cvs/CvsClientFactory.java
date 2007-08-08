package com.zutubi.pulse.scm.cvs;

import com.zutubi.pulse.scm.ScmClientFactory;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.cvs.config.CvsConfiguration;
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
            return objectFactory.buildBean(CvsClient.class, CONSTRUCTOR_ARGS,
                    new Object[]{config.getRoot(),
                            config.getModule(),
                            config.getPassword(),
                            config.getBranch()
                    }
            );
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
