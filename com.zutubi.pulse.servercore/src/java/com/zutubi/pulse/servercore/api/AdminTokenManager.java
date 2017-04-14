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

package com.zutubi.pulse.servercore.api;

import com.google.common.io.Files;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Manages a random, single-use admin token that is used by local command line
 * scripts to authenticate via the remote api.
 */
public class AdminTokenManager
{
    private static final Logger LOG = Logger.getLogger(AdminTokenManager.class);

    private static final String TOKEN_FILE = "admin.token";

    /**
     * A random token that allows a single admin login: the token is changed
     * each time it it used.
     */
    private String adminToken;
    private File configRoot;

    public static File getAdminTokenFilename(File configRoot)
    {
        return new File(configRoot, TOKEN_FILE);
    }

    /**
     * Init method. Make sure that we have an admin token available for use by
     * the command line.
     */
    public void init()
    {
        newRandomToken();
    }

    public boolean checkAdminToken(String token)
    {
        if (token.equals(adminToken))
        {
            // Matches the single-use admin token.  Allow login and generate
            // a new token.
            newRandomToken();
            return true;
        }
        return false;
    }

    private void newRandomToken()
    {
        File tokenFile = getAdminTokenFilename(configRoot);
        try
        {
            adminToken = RandomUtils.secureRandomString(128);
            Files.write(adminToken, tokenFile, Charset.defaultCharset());
        }
        catch (Exception e)
        {
            LOG.severe("Unable to create admin token file: " + e.getMessage(), e);
        }
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }
}
