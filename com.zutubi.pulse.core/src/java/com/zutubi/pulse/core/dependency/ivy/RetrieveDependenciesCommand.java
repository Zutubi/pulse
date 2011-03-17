package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunctionE;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.NullOutputStream;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.net.URL;

/**
 * A command that handles retrieving the dependencies for a build.  This
 * should run after the scm bootstrapping, but before the build.
 */
public class RetrieveDependenciesCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(RetrieveDependenciesCommand.class);

    public static final String COMMAND_NAME = "retrieve";
    public static final String OUTPUT_NAME = "retrieve output";
    public static final String IVY_REPORT_FILE = "ivyreport.xml";

    private IvyClient ivy;

    public RetrieveDependenciesCommand(RetrieveDependenciesCommandConfiguration config)
    {
        this.ivy = config.getIvy();
    }

    public void execute(final CommandContext commandContext)
    {
        try
        {
            final PulseExecutionContext context = (PulseExecutionContext) commandContext.getExecutionContext();

            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            AuthenticatedAction.execute(host, context.getSecurityHash(), new NullaryFunctionE<Object, Exception>()
            {
                public Object process() throws Exception
                {
                    OutputStream outputStream = context.getOutputStream();
                    if (outputStream == null)
                    {
                        outputStream = new NullOutputStream();
                    }
                    
                    final PrintWriter outputWriter = new PrintWriter(outputStream);
                    try
                    {
                        DefaultModuleDescriptor descriptor = context.getValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, DefaultModuleDescriptor.class);
                        String stageName = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_STAGE));
                        String retrievalPattern = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN));
                        boolean syncDestination = context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_SYNC_DESTINATION, DEFAULT_SYNC_DESTINATION);

                        String targetPattern = PathUtils.getPath(context.getWorkingDir().getAbsolutePath(), retrievalPattern);
                        IvyRetrievalReport retrievalReport = ivy.retrieveArtifacts(descriptor, stageName, targetPattern, syncDestination);

                        captureRetrievalReport(retrievalReport, commandContext);

                        if (retrievalReport.hasFailures())
                        {
                            // along with this recorded failure, ivy will report the details of the
                            // problems encountered in the builds log.
                            for (ArtifactDownloadReport failure : retrievalReport.getFailures())
                            {
                                commandContext.failure(failure.toString());
                            }
                        }
                        else
                        {
                            for (Artifact artifact : retrievalReport.getRetrievedArtifacts())
                            {
                                ModuleRevisionId mrid = artifact.getModuleRevisionId();
                                String targetPath = IvyPatternHelper.substitute(targetPattern, artifact);
                                String feedback = I18N.format("retrieve.feedback", mrid.getName(), artifact.getName(), mrid.getRevision(), targetPath);
                                outputWriter.println(feedback);
                            }
                        }
                        return null;
                    }
                    finally
                    {
                        // Don't close the writer: the underlying output stream
                        // is used by subsequent commands.
                        outputWriter.flush();

                        ivy.cleanup(); // cleanup the cached artifacts, they are not needed after they have been delivered.
                    }
                }
            });
        }
        catch (Exception e)
        {
            throw new BuildException("Error running dependency retrieval: " + e.getMessage(), e);
        }
    }

    private void captureRetrievalReport(IvyRetrievalReport retrievalReport, CommandContext commandContext) throws IOException, TransformerConfigurationException, SAXException
    {
        final File outDir = commandContext.registerArtifact(OUTPUT_NAME, null, false, false, null);

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
    }

    public void terminate()
    {

    }
}
