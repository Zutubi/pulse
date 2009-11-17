package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.i18n.Messages;
import com.zutubi.util.FileSystemUtils;
import static com.zutubi.util.FileSystemUtils.rmdir;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.util.MessageLogger;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

/**
 * The ivy client provides the core interface for interacting with ivy processes, encapsulating
 * all of the ivy process specific logic.
 * <p/>
 * The only ivy specific code that should be outside of this class relates to the configuration
 * of the repository and the various module descriptors.
 */
public class IvyClient
{
    private static final Messages I18N = Messages.getInstance(IvyClient.class);
    private static final Logger LOG = Logger.getLogger(IvyClient.class);

    private static final String RESOLVER_NAME = "pulse";
    private static final String SOURCEFILE = "sourcefile";

    private Ivy ivy;

    /**
     * Create a new instance of the ivy client using the specified ivy configuration.
     *
     * @param configuration the configuration used to setup the embedded ivy system.
     * @throws Exception if there are any problems setting up the IvyClient.
     */
    public IvyClient(IvyConfiguration configuration) throws Exception
    {
        if (configuration.getRepositoryBase() == null)
        {
            throw new IllegalArgumentException(I18N.format("configuration.repositoryBase.required"));
        }

        IvySettings settings = configuration.loadSettings();

        String repositoryBase = configuration.getRepositoryBase();

        AbstractPatternsBasedResolver resolver;
        if (isFile(repositoryBase))
        {
            resolver = new FileSystemResolver();

            // this resolver requires that the repository base be an absolute path when used in the patterns.
            repositoryBase = new File(new URI(repositoryBase)).getCanonicalPath();
        }
        else
        {
            resolver = new URLResolver();
        }

        resolver.setName(RESOLVER_NAME);
        resolver.addArtifactPattern(repositoryBase + "/" + configuration.getArtifactPattern());
        resolver.addIvyPattern(repositoryBase + "/" + configuration.getIvyPattern());
        resolver.setCheckmodified(true);

        settings.addResolver(resolver);
        settings.setDefaultResolver(RESOLVER_NAME);

        this.ivy = Ivy.newInstance(settings);
    }

    /**
     * Publish local artifacts to the ivy repository.  The artifacts to be published are
     * those defined in the module descriptor that match the specified confNames.
     * <p/>
     * The location of the artifact file to be published is defined by the extra attribute
     * 'sourcefile' which needs to be defined for each artifact being published.
     *
     * @param descriptor the descriptor that defines the artifacts to be published.
     * @param confNames  the configuration names identifying the set of artifacts to be published.  If
     *                   blank, all artifacts will be published.
     * @throws IOException if there is a failure to publish an artifact.
     */
    public void publishArtifacts(DefaultModuleDescriptor descriptor, String... confNames) throws IOException
    {
        URLHandler originalDefault = URLHandlerRegistry.getDefault();
        ivy.pushContext();
        try
        {
            URLHandlerRegistry.setDefault(new CustomURLHandler());

            DependencyResolver dependencyResolver = ivy.getSettings().getDefaultResolver();

            PublishOptions options = new PublishOptions();
            options.setOverwrite(true);
            options.setUpdate(true);
            options.setHaltOnMissing(true);

            if (confNames.length > 0)
            {
                options.setConfs(confNames);
            }

            Collection bad = checkCanPublishArtifacts(descriptor, confNames);
            if (bad.size() > 0)
            {
                throw new IOException(I18N.format("failure.publish.missingSourcefile"));
            }

            Collection missing = ivy.getPublishEngine().publish(descriptor, Arrays.asList("[" + SOURCEFILE + "]"), dependencyResolver, options);
            if (missing.size() > 0)
            {
                throw new IOException(I18N.format("failure.publish.general"));
            }
        }
        finally
        {
            ivy.popContext();
            URLHandlerRegistry.setDefault(originalDefault);
        }
    }

    // sanity check that 'sourcefile' is defined for the artifacts to be published.
    private Collection<Artifact> checkCanPublishArtifacts(DefaultModuleDescriptor descriptor, String... confNames) throws IOException
    {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        if (confNames.length > 0)
        {
            for (String confName : confNames)
            {
                artifacts.addAll(Arrays.asList(descriptor.getArtifacts(confName)));
            }
        }
        else
        {
            artifacts.addAll(Arrays.asList(descriptor.getAllArtifacts()));
        }

        List<Artifact> badArtifacts = new LinkedList<Artifact>();
        for (Artifact artifact : artifacts)
        {
            if (!artifact.getId().getExtraAttributes().containsKey(SOURCEFILE))
            {
                // we can not publish this artifact because we don't know what file to publish.
                badArtifacts.add(artifact);
            }
        }
        return badArtifacts;
    }

    /**
     * Publish the descriptor to the repository.
     *
     * @param descriptor to be published
     * @throws IOException if there are any errors publishing the descriptor.
     */
    public void publishDescriptor(DefaultModuleDescriptor descriptor) throws IOException
    {
        // we can only publish from a file, so we need to deliver the descriptor to a local file first.
        File tmp = null;
        // annoying but necessary.  See CustomURLHandler for details.
        URLHandler originalDefault = URLHandlerRegistry.getDefault();
        try
        {
            String revision = descriptor.getRevision();

            tmp = FileSystemUtils.createTempDir();
            File ivyFile = new File(tmp, "ivy.xml");
            XmlModuleDescriptorWriter.write(descriptor, ivyFile);

            ModuleRevisionId mrid = descriptor.getModuleRevisionId();

            PublishOptions options = new PublishOptions();
            options.setOverwrite(true);
            options.setUpdate(true);
            options.setHaltOnMissing(false); // we dont care about missing artifacts here, just that the ivy file gets published.
            options.setConfs(new String[]{"*"});
            options.setSrcIvyPattern(ivyFile.getCanonicalPath());

            if (StringUtils.stringSet(revision))
            {
                options.setPubrevision(revision);
            }

            URLHandlerRegistry.setDefault(new CustomURLHandler());
            ivy.publish(mrid, Collections.emptySet(), RESOLVER_NAME, options);
        }
        finally
        {
            URLHandlerRegistry.setDefault(originalDefault);

            if (tmp != null && !rmdir(tmp))
            {
                LOG.warning(I18N.format("warning.file.cleanup.failure", new Object[]{tmp.getCanonicalPath()}));
            }
        }
    }

    /**
     * Retrieve the artifacts defined by the dependencies in this descriptor, and write them to the
     * file system according to the target pattern.
     *
     * @param descriptor    the descriptor defining the dependencies to be retrieved.
     * @param targetPattern the pattern defining where the artifacts will be written.  This pattern,
     *                      supports the usual ivy tokens.
     * @return a retrieval report containing details of the artifacts that were retrieved.
     * @throws Exception if any problems are encountered.
     */
    public IvyRetrievalReport retrieveArtifacts(ModuleDescriptor descriptor, String targetPattern) throws IOException, ParseException
    {
        // annoying but necessary.  See CustomURLHandler for details.
        URLHandler originalDefault = URLHandlerRegistry.getDefault();
        try
        {
            IvyRetrievalReport report = new IvyRetrievalReport();

            ModuleRevisionId mrid = descriptor.getModuleRevisionId();

            String conf = "build";
            ResolveReport resolveReport = resolve(descriptor, conf);
            if (resolveReportHasProblems(resolveReport, report, conf))
            {
                return report;
            }

            RetrieveOptions options = new RetrieveOptions();
            options.setConfs(new String[]{conf});
            options.setSync(true); // important if the build directory is being re-used.

            URLHandlerRegistry.setDefault(new CustomURLHandler());
            ivy.retrieve(mrid, targetPattern, options);

            recordDownloadedArtifacts(targetPattern, report, mrid, options);

            return report;
        }
        finally
        {
            URLHandlerRegistry.setDefault(originalDefault);
        }
    }

    private boolean resolveReportHasProblems(ResolveReport resolveReport, IvyRetrievalReport report, String confName)
    {
        ConfigurationResolveReport configurationResolveReport = resolveReport.getConfigurationReport(confName);
        if (configurationResolveReport.hasError())
        {
            List<IvyNode> dependencies = resolveReport.getDependencies();
            for (IvyNode dependency : dependencies)
            {
                if (dependency.hasProblem()) // this problem flag seems to be isolated to resolution problems.
                {
                    Artifact artifact = new DefaultArtifact(dependency.getResolvedId(), null, "?", "?", "?");
                    ArtifactDownloadReport artifactDownloadReport = new ArtifactDownloadReport(artifact);
                    artifactDownloadReport.setDownloadStatus(DownloadStatus.FAILED);
                    String errMsg = dependency.getProblemMessage();
                    if (errMsg.length() > 0)
                    {
                        artifactDownloadReport.setDownloadDetails("unresolved dependency: " + dependency.getId() + ": " + errMsg);
                    }
                    else
                    {
                        artifactDownloadReport.setDownloadDetails("unresolved dependency: " + dependency.getId());
                    }
                    report.addDownloadReports(artifactDownloadReport);
                }
                else
                {
                    // Although the dependency didn't have any problems, the download reports may have,
                    // for example if the modules artifacts could not be located.
                    ArtifactDownloadReport[] downloadReports = configurationResolveReport.getDownloadReports(dependency.getResolvedId());
                    if (downloadReports.length > 0)
                    {
                        report.addDownloadReports(downloadReports);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void recordDownloadedArtifacts(String targetPattern, IvyRetrievalReport report, ModuleRevisionId mrid, RetrieveOptions options) throws ParseException, IOException
    {
        Map<ArtifactDownloadReport, Set<String>> artifactDownloadReports = ivy.getRetrieveEngine().determineArtifactsToCopy(mrid, targetPattern, options);
        for (final ArtifactDownloadReport artifactDownloadReport : artifactDownloadReports.keySet())
        {
            report.addDownloadReports(artifactDownloadReport);
        }
    }

    /**
     * Resolve the descriptors dependencies, identifying the artifacts described and
     * caching the result.
     *
     * @param descriptor the descriptor whos dependencies are being resovled.
     * @param confNames  the configuration names that define the set of dependencies to be resolved.
     * @return a resolve report.
     * @throws Exception on error.
     */
    public ResolveReport resolve(ModuleDescriptor descriptor, String... confNames) throws IOException, ParseException
    {
        ResolveOptions options = new ResolveOptions();
        options.setValidate(ivy.getSettings().doValidate());
        if (confNames.length > 0)
        {
            options.setConfs(confNames);
        }
        else
        {
            options.setConfs(new String[]{"*"});
        }
        options.setCheckIfChanged(true);
        options.setUseCacheOnly(false);
        return ivy.resolve(descriptor, options);
    }

    /**
     * Set this message logger to receive all future logging messages from ivy.
     * This logger will continue to receive messages until it is popped.
     *
     * @param logger the logger to receive logging messages.
     * @see #popMessageLogger()
     */
    public void pushMessageLogger(MessageLogger logger)
    {
        ivy.getLoggerEngine().pushLogger(logger);
    }

    public void popMessageLogger()
    {
        ivy.getLoggerEngine().popLogger();
    }

    private boolean isFile(String path)
    {
        try
        {
            return new URI(path).getScheme().equals("file");
        }
        catch (URISyntaxException e)
        {
            return false;
        }
    }
}
