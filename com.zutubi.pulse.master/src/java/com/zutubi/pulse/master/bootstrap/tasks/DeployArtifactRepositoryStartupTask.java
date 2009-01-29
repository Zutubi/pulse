package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.servercore.jetty.*;

import java.io.File;

import org.mortbay.jetty.Server;
import org.acegisecurity.Authentication;

/**
 * Startup task responsible for starting the artifact repository.
 */
public class DeployArtifactRepositoryStartupTask implements StartupTask
{
    private JettyServerManager jettyServerManager;
    private ConfigurationManager configurationManager;
    private SecurityHandler artifactRepositorySecurityHandler;

    public void execute() throws Exception
    {
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        ArtifactRepositoryConfigurationHandler repository = new ArtifactRepositoryConfigurationHandler();
        repository.setHost(sysConfig.getBindAddress());
        repository.setPort(8888); // need to make this configurable.

        // ignore the configured security handler for now.
        SecurityHandler accessAllowed = new SecurityHandler();
        accessAllowed.setPrivilegeEvaluator(new PrivilegeEvaluator()
        {
            public boolean isAllowed(HttpInvocation invocation, Authentication auth)
            {
                return true;
            }
        });
        repository.setSecurityHandler(accessAllowed);

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

    public void setArtifactRepositorySecurityHandler(SecurityHandler artifactRepositorySecurityHandler)
    {
        this.artifactRepositorySecurityHandler = artifactRepositorySecurityHandler;
    }
}
