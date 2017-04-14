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

package com.zutubi.pulse.servercore.services;

import com.google.common.io.Files;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.UserPaths;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

/**
 */
public class ServiceTokenManagerTest extends PulseTestCase
{
    private static final String TEST_TOKEN = "test token string";

    private File tempDir;
    private ServiceTokenManager tokenManager;

    private ConfigurationManager configManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(ServiceTokenManager.class.getName(), "");
        tokenManager = new ServiceTokenManager();
        UserPaths paths = new UserPaths()
        {
            public File getData()
            {
                return tempDir;
            }

            public File getUserConfigRoot()
            {
                return tempDir;
            }
        };

        configManager = mock(ConfigurationManager.class);
        stub(configManager.getUserPaths()).toReturn(paths);
        tokenManager.setConfigurationManager(configManager);
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testGeneratesToken()
    {
        assertNull(tokenManager.getToken());
        tokenManager.init();
        assertNotNull(tokenManager.getToken());
    }

    public void testUsesExistingToken() throws IOException
    {
        assertNull(tokenManager.getToken());
        File tokenFile = tokenManager.getTokenFile();
        Files.write(TEST_TOKEN, tokenFile, Charset.defaultCharset());
        tokenManager.init();
        assertEquals(TEST_TOKEN, tokenManager.getToken());
    }

    public void testAcceptsToken()
    {
        tokenManager.init();
        String token = tokenManager.getToken();
        tokenManager.validateToken(token);
    }

    public void testRejectsInvalidToken()
    {
        tokenManager.init();
        String token = tokenManager.getToken();
        try
        {
            tokenManager.validateToken(token + "invalid");
            fail();
        }
        catch (InvalidTokenException e)
        {
        }
    }

    public void testAcceptsFirstToken()
    {
        tokenManager.setGenerate(false);
        tokenManager.init();
        assertNull(tokenManager.getToken());
        tokenManager.validateToken(TEST_TOKEN);
        assertEquals(TEST_TOKEN, tokenManager.getToken());
    }

    public void testGeneratedTokenPersists()
    {
        tokenManager.init();
        String token = tokenManager.getToken();

        ServiceTokenManager another = new ServiceTokenManager();
        another.setConfigurationManager(configManager);
        assertNull(another.getToken());
        another.init();
        assertEquals(token, another.getToken());
    }

    public void testAcceptedTokenPersists()
    {
        tokenManager.setGenerate(false);
        tokenManager.init();
        tokenManager.validateToken(TEST_TOKEN);

        ServiceTokenManager another = new ServiceTokenManager();
        another.setConfigurationManager(configManager);
        assertNull(another.getToken());
        another.init();
        assertEquals(TEST_TOKEN, another.getToken());
    }

    public void testTokenRefreshes()
    {
        tokenManager.setGenerate(false);
        tokenManager.init();
        tokenManager.validateToken(TEST_TOKEN);

        try
        {
            tokenManager.validateToken(TEST_TOKEN + "foo");
            fail();
        }
        catch(InvalidTokenException e)
        {
        }

        tokenManager.getTokenFile().delete();
        tokenManager.validateToken(TEST_TOKEN + "foo");
        assertEquals(TEST_TOKEN + "foo", tokenManager.getToken());
    }
}
