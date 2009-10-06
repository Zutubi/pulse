package com.zutubi.pulse.core.scm.noop;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.noop.config.NoopScmConfiguration;

/**
 * Factory to build noop SCMs.
 */
public class NoopScmClientFactory implements ScmClientFactory<NoopScmConfiguration>
{
    public ScmClient createClient(NoopScmConfiguration config) throws ScmException
    {
        return new NoopScmClient();
    }
}
