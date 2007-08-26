package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMCancelledException;
import com.zutubi.pulse.scm.SCMCheckoutEventHandler;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;
import com.zutubi.pulse.util.ForkOutputStream;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 */
public abstract class ScmBootstrapper implements Bootstrapper, SCMCheckoutEventHandler
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String agent;
    protected String project;
    protected String spec;
    protected Scm scm;
    protected BuildRevision revision;
    protected boolean terminated = false;
    protected transient PrintWriter outputWriter;

    public ScmBootstrapper(String project, String spec, Scm scm, BuildRevision revision)
    {
        this.project = project;
        this.spec = spec;
        this.scm = scm;
        this.revision = revision;
    }

    public void prepare(String agent)
    {
        this.agent = agent;
    }

    public void bootstrap(CommandContext context)
    {
        File workDir;

        if (scm.getPath() != null)
        {
            workDir = new File(context.getPaths().getBaseDir(), scm.getPath());
        }
        else
        {
            workDir = context.getPaths().getBaseDir();
        }

        File outDir = new File(context.getOutputDir(), BootstrapCommand.OUTPUT_NAME);
        outDir.mkdirs();

        OutputStream out = null;
        FileOutputStream fout = null;
        SCMServer server = null;

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
            server = bootstrap(workDir);
        }
        catch (IOException e)
        {
            SCMServerUtils.close(server);
            throw new BuildException("I/O error running bootstrap: " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(outputWriter);
        }

        if (server != null)
        {
            try
            {
                server.writeConnectionDetails(outDir);
                context.getGlobalScope().add(server.getConnectionProperties(getId(), workDir));
            }
            catch (Exception e)
            {
                LOG.warning("Unable to capture SCM connection details: " + e.getMessage(), e);
            }
            finally
            {
                SCMServerUtils.close(server);
            }
        }
    }

    protected String getId()
    {
        return project + "-" + spec + "-" + agent;
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

    public void checkCancelled() throws SCMCancelledException
    {
        if (terminated)
        {
            throw new SCMCancelledException("Operation cancelled");
        }
    }

    public void terminate()
    {
        terminated = true;
    }

    abstract SCMServer bootstrap(File workDir);
}
