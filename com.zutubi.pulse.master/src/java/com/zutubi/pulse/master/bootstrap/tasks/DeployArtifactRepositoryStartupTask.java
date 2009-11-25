package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.jetty.ArtifactRepositoryConfigurationHandler;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.util.StringUtils;
import org.mortbay.http.HttpHandler;

import java.io.File;

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
        ArtifactRepositoryConfigurationHandler repository = new ArtifactRepositoryConfigurationHandler();
        repository.setSecurityHandler(securityHandler);

        File repositoryBase = ((MasterUserPaths)configurationManager.getUserPaths()).getRepositoryRoot();
        ensureIsDirectory(repositoryBase);

        repository.setBase(repositoryBase); // need to make this configurable.

        String contextPath = StringUtils.join("/", true, true, configurationManager.getSystemConfig().getContextPath(), WebManager.REPOSITORY_PATH);
        jettyServerManager.configureContext(WebManager.WEBAPP_PULSE, contextPath, repository);
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
