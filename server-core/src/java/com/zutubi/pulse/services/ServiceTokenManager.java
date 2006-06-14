package com.zutubi.pulse.services;

import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;

/**
 */
public class ServiceTokenManager
{
    private static final Logger LOG = Logger.getLogger(ServiceTokenManager.class);

    private static final String TOKEN_FILE = "service.token";

    /**
     * Indicates the policy when no token exists.  If true, a new random
     * token will be generated.  If false, the first token that is valiated
     * will be accepted and stored.
     */
    private boolean generate = true;

    private SystemPaths paths;
    private String token;

    public void init()
    {
        File tokenFile = getTokenFile();
        if(tokenFile.exists())
        {
            try
            {
                token = IOUtils.fileToString(tokenFile);
            }
            catch (IOException e)
            {
                LOG.severe("Unable to read service token: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
            }
        }
        else if(generate)
        {
            this.token = RandomUtils.randomString(64);
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
            throw new InvalidTokenException();
        }
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
            FileSystemUtils.createFile(tokenFile, token);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write token file: " + tokenFile.getAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    File getTokenFile()
    {
        return new File(paths.getConfigRoot(), TOKEN_FILE);
    }

    public void setGenerate(boolean generate)
    {
        this.generate = generate;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.paths = configurationManager.getSystemPaths();
    }

}
