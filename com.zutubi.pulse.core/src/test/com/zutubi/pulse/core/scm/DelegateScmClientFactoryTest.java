package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.TestScmConfiguration;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;

public class DelegateScmClientFactoryTest extends ZutubiTestCase
{
    private DelegateScmClientFactory factory;

    protected void setUp() throws Exception
    {
        super.setUp();

        factory = new DelegateScmClientFactory();
        ScmExtensionManager extensionManager = new ScmExtensionManager();
        extensionManager.setObjectFactory(new DefaultObjectFactory());
        extensionManager.registerClientFactory(TestScmConfiguration.class, TestScmClientFactory.class);
        factory.setScmExtensionManager(extensionManager);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testCreateClient() throws ScmException
    {
        ScmClient client = factory.createClient(new TestScmConfiguration());
        assertNotNull(client);
        try
        {
            assertTrue(client instanceof TestScmClient);
        }
        finally
        {
            client.close();
        }
    }

    public void testDataCache() throws ScmException
    {
        TestScmClient client = (TestScmClient) factory.createClient(new TestScmConfiguration());
        assertNotNull(client.cache);
        client.cache.put("key", "value");

        TestScmClient anotherClient = (TestScmClient) factory.createClient(new TestScmConfiguration());
        assertNotNull(anotherClient.cache);
        assertEquals("value", anotherClient.cache.get("key"));

        client.close();
        anotherClient.close();
    }
}
