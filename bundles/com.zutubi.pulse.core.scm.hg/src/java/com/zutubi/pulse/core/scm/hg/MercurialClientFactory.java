package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.hg.config.MercurialConfiguration;

/**
 * Implementation of the {@link com.zutubi.pulse.core.scm.api.ScmClientFactory} to handle the
 * creation of the Mercurial ScmClient.
 *
 * @see com.zutubi.pulse.core.scm.hg.MercurialClient
 */
public class MercurialClientFactory implements ScmClientFactory<MercurialConfiguration>
{
    public ScmClient createClient(MercurialConfiguration config) throws ScmException
    {
        return new MercurialClient(config);
    }
}
