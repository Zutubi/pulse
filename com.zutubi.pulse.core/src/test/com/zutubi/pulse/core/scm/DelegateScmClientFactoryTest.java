/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    }
