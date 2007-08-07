package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.scm.ScmCancelledException;
import com.zutubi.pulse.scm.ScmCheckoutEventHandler;
import com.zutubi.pulse.servercore.scm.config.ScmConfiguration;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.util.ForkOutputStream;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 */
public abstract class ScmBootstrapper implements Bootstrapper, ScmCheckoutEventHandler
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String agent;
    protected String project;
    protected ScmConfiguration scm;
    protected BuildRevision revision;
    protected boolean terminated = false;
    protected transient PrintWriter outputWriter;

    public ScmBootstrapper(String project, ScmConfiguration scm, BuildRevision revision)
    {
        this.project = project;
        this.scm = scm;
        this.revision = revision;
    }

    public void prepare(String agent)
    {
        this.agent = agent;
    }

    public void bootstrap(CommandContext context)
    {
        File workDir = context.getPaths().getBaseDir();
        File outDir = new File(context.getOutputDir(), BootstrapCommand.OUTPUT_NAME);
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
            client = bootstrap(workDir);
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
                context.getGlobalScope().add(client.getProperties(getId(), workDir));
            }
            catch (Exception e)
            {
                LOG.warning("Unable to capture SCM connection details: " + e.getMessage(), e);
            }
        }
    }

    protected String getId()
    {
        return project + "-" + agent;
    }

    public void status(String message)
    {
        outputWriter.println(message);
    }

    public void fileCheckedOut(Change change)
    {
        String revision = "";
        if (change.getRevision() != null)
        {
            revision = "#" + change.getRevision();
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

    abstract ScmClient bootstrap(File workDir);
}
