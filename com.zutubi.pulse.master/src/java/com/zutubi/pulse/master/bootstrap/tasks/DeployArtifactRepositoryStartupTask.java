package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.*;

import java.io.File;
import java.io.IOException;

import org.mortbay.jetty.Server;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;

/**
 * Startup task responsible for starting the artifact repository.
 */
public class DeployArtifactRepositoryStartupTask implements StartupTask
{
    private JettyServerManager jettyServerManager;
    private ConfigurationManager configurationManager;
    private HttpHandler securityHandler;

    public void execute() throws Exception
    {
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        ArtifactRepositoryConfigurationHandler repository = new ArtifactRepositoryConfigurationHandler();
        repository.setHost(sysConfig.getBindAddress());
        repository.setPort(8888); // need to make this configurable.

        repository.setSecurityHandler(securityHandler);

        File repositoryBase = new File(configurationManager.getUserPaths().getData(), "repository");
        ensureIsDirectory(repositoryBase);

        repository.setBase(repositoryBase); // need to make this configurable.

        Server server = jettyServerManager.createNewServer("repository", repository);
        server.start();
    }

    private void ensureIsDirectory(File dir)
    {
        if (dir.exists() && !dir.isDirectory())
        {
            throw new RuntimeException("'" + dir.getAbsolutePath() + "' already exists but is not a directory as expected.");
        }
        if (!dir.exists() && !dir.mkdirs())
        {
            throw new RuntimeException("Failed to create new directory at '" + dir.getAbsolutePath() + "'");
        }
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setArtifactRepositorySecurityHandler(HttpHandler securityHandler)
    {
        this.securityHandler = securityHandler;
    }
}
