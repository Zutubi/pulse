package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.*;
import static com.zutubi.pulse.core.BuildProperties.NAMESPACE_INTERNAL;
import static com.zutubi.pulse.core.BuildProperties.PROPERTY_OUTPUT_DIR;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.io.ForkOutputStream;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 */
public abstract class ScmBootstrapper implements Bootstrapper, ScmEventHandler
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String project;
    protected ScmConfiguration scmConfig;
    protected BuildRevision revision;
    protected boolean terminated = false;
    protected transient PrintWriter outputWriter;

    public ScmBootstrapper(String project, ScmConfiguration scmConfig, BuildRevision revision)
    {
        this.project = project;
        this.scmConfig = scmConfig;
        this.revision = revision;
    }

    public void bootstrap(ExecutionContext context)
    {
        File outDir = new File(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR), BootstrapCommand.OUTPUT_NAME);
        outDir.mkdirs();

        OutputStream out;
        FileOutputStream fout = null;
        ScmClient client = null;

        try
        {
            fout = new FileOutputStream(new File(outDir, BootstrapCommand.FILES_FILE));
            if (context.getOutputStream() == null)
            {
                out = fout;
            }
            else
            {
                out = new ForkOutputStream(fout, context.getOutputStream());
            }

            outputWriter = new PrintWriter(out);
            client = doBootstrap(context);
        }
        catch (IOException e)
        {
            throw new BuildException("I/O error running bootstrap: " + e.getMessage(), e);
        }
        finally
        {
            // close the file output stream, but not the contexts output stream. That
            // will still be used further on in the build, it is not ours to close.
            outputWriter.flush();
            IOUtils.close(fout);
        }

        if (client != null)
        {
            try
            {
                client.storeConnectionDetails(outDir);
            }
            catch (Exception e)
            {
                LOG.warning("Unable to capture SCM connection details: " + e.getMessage(), e);
            }
            finally
            {
                ScmClientUtils.close(client);
            }
        }
    }

    protected String getId(ExecutionContext buildContext)
    {
        return project + "-" + buildContext.getString(BuildProperties.NAMESPACE_INTERNAL, BuildProperties.PROPERTY_AGENT);
    }

    public void status(String message)
    {
        outputWriter.println(message);
    }

    public void fileChanged(Change change)
    {
        String revision = "";
        if (change.getRevisionString() != null)
        {
            revision = "#" + change.getRevisionString();
        }

        outputWriter.println(change.getFilename() + revision + " - " + change.getAction().toString());
    }

    public void checkCancelled() throws ScmCancelledException
    {
        if (terminated)
        {
            throw new ScmCancelledException("Operation cancelled");
        }
    }

    public void terminate()
    {
        terminated = true;
    }

    protected ScmClient createScmClient(ExecutionContext executionContext) throws ScmException
    {
        ScmClientFactory scmClientFactory = executionContext.getValue(NAMESPACE_INTERNAL, BuildProperties.PROPERTY_SCM_CLIENT_FACTORY, ScmClientFactory.class);
        return scmClientFactory.createClient(scmConfig);
    }

    abstract ScmClient doBootstrap(ExecutionContext executionContext);
}
