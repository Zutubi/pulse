package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.TestScmConfiguration;


/**
 * A factory that creates {@link TestScmClient} instances.
 */
public class TestScmClientFactory implements ScmClientFactory<TestScmConfiguration>
{
    public ScmClient createClient(TestScmConfiguration config) throws ScmException
    {
        return new TestScmClient();
    }
}
