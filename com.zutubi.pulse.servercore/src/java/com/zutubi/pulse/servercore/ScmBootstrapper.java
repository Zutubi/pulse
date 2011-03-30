package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapCommand;
import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import static com.zutubi.pulse.core.engine.api.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 */
public abstract class ScmBootstrapper extends BootstrapperSupport implements ScmFeedbackHandler
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String project;
    protected BuildRevision revision;
    protected transient PrintWriter filesWriter;

    public ScmBootstrapper(String project, BuildRevision revision)
    {
        this.project = project;
        this.revision = revision;
    }

    public void doBootstrap(CommandContext commandContext)
    {
        ExecutionContext context = commandContext.getExecutionContext();
        File outDir = new File(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR), BootstrapCommand.OUTPUT_NAME);
        if (!outDir.isDirectory() && !outDir.mkdirs())
        {
            throw new BuildException("Failed to create output directory: " + outDir);
        }

        ScmClient client = null;

        try
        {
            filesWriter = new PrintWriter(new FileOutputStream(new File(outDir, BootstrapCommand.FILES_FILE)));
            client = doBootstrap(context);
        }
        catch (IOException e)
        {
            throw new BuildException("I/O error running bootstrap: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.flush(filesWriter);
            IOUtils.close(filesWriter);
        }

        if (client != null)
        {
            try
            {
                // These properties will already be in the context as sent from
                // the master.  We reset them here because:
                //   1) They may be machine-dependent (e.g. Perforce ticket logins).
                //   2) We want the build to see exactly what we used to bootstrap.
                PulseExecutionContext pulseContext = (PulseExecutionContext) context;
                List<ResourceProperty> properties = client.getProperties(context);
                for (ResourceProperty property: properties)
                {
                    pulseContext.update(property);
                }
                
                client.storeConnectionDetails(context, outDir);
            }
            catch (Exception e)
            {
                LOG.warning("Unable to capture SCM connection details: " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(client);
            }
        }
    }

    public void status(String message)
    {
        filesWriter.println(message);
        writeFeedback(message);
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if (isTerminated())
        {
            throw new ScmCancelledException("Operation cancelled");
        }
    }

    protected ScmClient createScmClient(ExecutionContext executionContext) throws ScmException
    {
        ScmConfiguration scmConfig = executionContext.getValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_SCM_CONFIGURATION, ScmConfiguration.class);
        ScmClientFactory scmClientFactory = executionContext.getValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_SCM_CLIENT_FACTORY, ScmClientFactory.class);
        return scmClientFactory.createClient(scmConfig);
    }

    abstract ScmClient doBootstrap(ExecutionContext executionContext);
}
