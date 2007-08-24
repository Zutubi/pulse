package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapCommand;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.util.ForkOutputStream;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 */
public abstract class ScmBootstrapper implements Bootstrapper, ScmEventHandler
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String agent;
    protected String project;
    protected ScmClient scm;
    protected BuildRevision revision;
    protected boolean terminated = false;
    protected transient PrintWriter outputWriter;

    public ScmBootstrapper(String project, ScmClient scm, BuildRevision revision)
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

        ScmContext scmContext = new ScmContext();
        scmContext.setDir(workDir);
        
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
            client = bootstrap(scmContext);
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

                scmContext.getProperties();

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

    abstract ScmClient bootstrap(ScmContext context);
}
