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
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.UserPaths;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 */
public class ServiceTokenManager implements TokenManager
{
    private static final Logger LOG = Logger.getLogger(ServiceTokenManager.class);

    private static final int TOKEN_LENGTH = 64;
    private static final String TOKEN_FILE = "service.token";

    /**
     * Indicates the policy when no token exists.  If true, a new random
     * token will be generated.  If false, the first token that is valiated
     * will be accepted and stored.
     */
    private boolean generate = true;

    private UserPaths paths;
    private String token;

    public void init()
    {
        File tokenFile = getTokenFile();
        if(tokenFile.exists())
        {
            try
            {
                token = Files.toString(tokenFile, Charset.defaultCharset());
            }
            catch (IOException e)
            {
                LOG.severe("Unable to read service token: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
        else if(generate)
        {
            token = RandomUtils.secureRandomString(TOKEN_LENGTH);

            writeToken();
        }
    }

    public void validateToken(String token) throws InvalidTokenException
    {
        if(this.token == null && !generate)
        {
            this.token = token;
            writeToken();
        }

        if(!token.equals(this.token))
        {
            if(!generate)
            {
                // Check if the token file has been remove, we will refresh if so.
                if(checkRefreshed(token))
                {
                    return;
                }
            }
            throw new InvalidTokenException();
        }
    }

    private boolean checkRefreshed(String token)
    {
        File tokenFile = getTokenFile();
        if(!tokenFile.exists())
        {
            this.token = token;
            writeToken();
            return true;
        }

        return false;
    }

    public String getToken()
    {
        return token;
    }

    private void writeToken()
    {
        File tokenFile = getTokenFile();
        try
        {
            Files.write(token, tokenFile, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write token file: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    File getTokenFile()
    {
        return new File(paths.getUserConfigRoot(), TOKEN_FILE);
    }

    public void setGenerate(boolean generate)
    {
        this.generate = generate;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.paths = configurationManager.getUserPaths();
    }
}
