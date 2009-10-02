package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.api.Command;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import static com.zutubi.pulse.core.engine.api.BuildProperties.*;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.NullaryFunctionE;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.report.XmlReportParser;

import java.io.File;
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

            commandContext.registerArtifact(OUTPUT_NAME, null);
            final File outDir = new File(context.getFile(NAMESPACE_INTERNAL, PROPERTY_OUTPUT_DIR), OUTPUT_NAME);

            URL masterUrl = new URL(context.getString(NAMESPACE_INTERNAL, PROPERTY_MASTER_URL));
            String host = masterUrl.getHost();

            AuthenticatedAction.execute(host, context.getSecurityHash(), new NullaryFunctionE<Object, Exception>()
            {
                public Object process() throws Exception
                {
                    ModuleDescriptor descriptor = context.getValue(NAMESPACE_INTERNAL, PROPERTY_DEPENDENCY_DESCRIPTOR, ModuleDescriptor.class);
                    ModuleRevisionId mrid = descriptor.getModuleRevisionId();
                    String retrievalPattern = context.resolveVariables(context.getString(NAMESPACE_INTERNAL, PROPERTY_RETRIEVAL_PATTERN));

                    if (!ivy.isResolved(mrid))
                    {
                        ivy.resolve(descriptor);
                    }
                    ivy.retrieve(mrid, context.getWorkingDir().getAbsolutePath() + "/" + retrievalPattern);

                    // capture the resolve report for later processing.
                    ResolutionCacheManager cacheMgr = ivy.getResolutionCacheManager();

                    String resolveId = ResolveOptions.getDefaultResolveId(new ModuleId(mrid.getOrganisation(), mrid.getName()));
                    File xml = cacheMgr.getConfigurationResolveReportInCache(resolveId, IvyClient.CONFIGURATION_BUILD);

                    File ivyReport = new File(outDir, IVY_REPORT_FILE);
                    if (!outDir.isDirectory() && !outDir.mkdirs())
                    {
                        throw new BuildException("Failed to create command output directory: " + outDir.getCanonicalPath());
                    }

                    FileSystemUtils.copy(ivyReport, xml);

                    XmlReportParser parser = new XmlReportParser();
                    parser.parse(ivyReport);
                    

                    return null;
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
