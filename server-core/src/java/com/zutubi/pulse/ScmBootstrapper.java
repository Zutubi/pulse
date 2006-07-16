package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapCommand;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.core.model.Change;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 * 
 */
public abstract class ScmBootstrapper implements Bootstrapper
{
    private static final Logger LOG = Logger.getLogger(ScmBootstrapper.class);

    protected String agent;
    protected String project;
    protected String spec;
    protected Scm scm;
    protected BuildRevision revision;

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

        List<Change> changes = bootstrap(workDir);
        if (changes.size() > 0)
        {
            writeChanges(changes, new File(outDir, BootstrapCommand.FILES_FILE));
        }

        try
        {
            scm.createServer().writeConnectionDetails(outDir);
        }
        catch (Exception e)
        {
            LOG.warning("Unable to capture SCM connection details: " + e.getMessage(), e);
        }
    }

    private void writeChanges(List<Change> changes, File file)
    {
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(file);
            for(Change change: changes)
            {
                writeChange(change, writer);
            }
        }
        catch (IOException e)
        {
            LOG.warning("Unable to capture change information to file '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(writer);
        }
    }

    private void writeChange(Change change, PrintWriter writer)
    {
        String revision = "";
        if(change.getRevision() != null)
        {
            revision = "#" + change.getRevision();
        }

        writer.println(change.getFilename() + revision + " - " + change.getAction().toString());
    }

    protected String getId()
    {
        return project + "-" + spec + "-" + agent;
    }

    abstract List<Change> bootstrap(File workDir);

}
