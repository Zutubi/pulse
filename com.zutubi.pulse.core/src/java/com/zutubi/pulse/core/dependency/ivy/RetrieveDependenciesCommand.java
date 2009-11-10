package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunctionE;
import com.zutubi.util.io.IOUtils;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;

/**
 * A command that handles retrieving the dependencies for a build.  This
 * should run after the scm bootstrapping, but before the build.
 */
public class RetrieveDependenciesCommand implements Command
{
    public static final String COMMAND_NAME = "retrieve";
    public static final String OUTPUT_NAME = "retrieve output";
    public static final String IVY_REPORT_FILE = "ivyreport.xml";

    private IvyClient ivy;

    public RetrieveDependenciesCommand(RetrieveDependenciesCommandConfiguration config)
    {
        this.ivy = config.getIvy();
    }

    public void execute(CommandContext commandContext)
    {
        try
        {
            final PulseExecutionContext context = (PulseExecutionContext) commandContext.getExecutionContext();

            final File outDir = commandContext.registerArtifact(OUTPUT_NAME, null);

            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            AuthenticatedAction.execute(host, context.getSecurityHash(), new NullaryFunctionE<Object, Exception>()
            {
                public Object process() throws Exception
                {
                    final PrintWriter outputWriter = new PrintWriter(context.getOutputStream());
                    try
                    {
                        ModuleDescriptor descriptor = context.getValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
                        String retrievalPattern = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN));

                        String targetPattern = PathUtils.getPath(context.getWorkingDir().getAbsolutePath(), retrievalPattern);
                        IvyRetrievalReport retrievalReport = ivy.retrieveArtifacts(descriptor, targetPattern);
                        for (Artifact artifact : retrievalReport.getArtifacts())
                        {
                            outputWriter.println(IvyPatternHelper.substitute(targetPattern, artifact));
                        }

                        File reportFile = new File(outDir, IVY_REPORT_FILE);
                        if (!outDir.isDirectory() && !outDir.mkdirs())
                        {
                            throw new BuildException("Failed to create command output directory: " + outDir.getCanonicalPath());
                        }

                        FileOutputStream output = null;
                        try
                        {
                            output = new FileOutputStream(reportFile);
                            retrievalReport.toXml(output);
                        }
                        finally
                        {
                            IOUtils.close(output);
                        }

                        return null;
                    }
                    finally
                    {
                        IOUtils.close(outputWriter);
                    }
                }
            });
        }
        catch (Exception e)
        {
            throw new BuildException("Error running dependency retrieval: " + e.getMessage(), e);
        }
    }

    public void terminate()
    {

    }
}
