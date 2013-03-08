package com.zutubi.pulse.core.dependency.ivy;

import com.google.common.io.ByteStreams;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.concurrent.Callable;

import static com.zutubi.pulse.core.engine.api.BuildProperties.*;

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

            AuthenticatedAction.execute(host, context.getSecurityHash(), new Callable<Object>()
            {
                public Object call() throws Exception
                {
                    OutputStream outputStream = context.getOutputStream();
                    if (outputStream == null)
                    {
                        outputStream = ByteStreams.nullOutputStream();
                    }
                    
                    final PrintWriter outputWriter = new PrintWriter(outputStream);
                    try
                    {
                        DefaultModuleDescriptor descriptor = context.getValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, DefaultModuleDescriptor.class);
                        String stageName = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_STAGE));
                        String retrievalPattern = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN));
                        boolean syncDestination = context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_SYNC_DESTINATION, DEFAULT_SYNC_DESTINATION);
                        boolean unzip = context.getBoolean(NAMESPACE_INTERNAL, PROPERTY_UNZIP_RETRIEVED_ARCHIVES, false);
                        
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
                            outputWriter.println();
                            outputWriter.println(I18N.format("retrieve.summary"));
                            
                            for (Artifact artifact : retrievalReport.getRetrievedArtifacts())
                            {
                                ModuleRevisionId mrid = artifact.getModuleRevisionId();
                                String targetPath = IvyPatternHelper.substitute(targetPattern, artifact);
                                outputWriter.println(I18N.format("retrieve.feedback", mrid.getName(), artifact.getName(), mrid.getRevision(), targetPath));
                                File targetFile = new File(targetPath);
                                if (unzip && targetPath.endsWith(".zip") && targetFile.isFile())
                                {
                                    File parentDir = targetFile.getParentFile();
                                    outputWriter.println(I18N.format("unzipping", parentDir.getAbsolutePath()));
                                    PulseZipUtils.extractZip(targetFile, parentDir);
                                    outputWriter.println(I18N.format("unzipped"));
                                    FileSystemUtils.delete(targetFile);
                                }
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
