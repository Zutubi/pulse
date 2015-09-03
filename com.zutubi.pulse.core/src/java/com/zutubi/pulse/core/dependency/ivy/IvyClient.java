package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.i18n.Messages;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.reflection.ReflectionUtils;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ConfigurationResolveReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.zutubi.util.io.FileSystemUtils.rmdir;

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
    private static final Logger   LOG  = Logger.getLogger(IvyClient.class);

    /**
     * After reports of "bad ivy file in cache" that suspiciously seem to come in pairs, I tested
     * some Ivy operations from multiple threads.  I found multiple thread-safety issues, which
     * manifest in different ways but often around corrupt descriptors.  Once of these is a classic
     * use of SimpleDateFormat, which is fixed in later Ivy versions, but I don't believe all
     * problems are down to that.  So it seems best just to serialise all Ivy operations in the
     * same process.  Hence this shared lock.
     * <p/>
     * Note that some operations in this class involve network and file I/O, so serialisation
     * could at times cause bottlenecks.  I hope this will not be a big issue, as the slower
     * operations only happen on agents where concurrency is minimal.  On the master there will be
     * more contention, but everything happens on the local file system so will hopefully not be
     * too slow.
     */
    private static final Lock LOCK = new ReentrantLock();

    private static final String RESOLVER_NAME = "pulse";

    private Ivy              ivy;
    private IvyConfiguration configuration;

    /**
     * Create a new instance of the ivy client using the specified ivy configuration.
     *
     * @param configuration the configuration used to setup the embedded ivy system.
     * @throws Exception if there are any problems setting up the IvyClient.
     */
    public IvyClient(IvyConfiguration configuration) throws Exception
    {
        LOCK.lock();
        try
        {
            if (configuration.getRepositoryBase() == null)
            {
                throw new IllegalArgumentException(I18N.format("configuration.repositoryBase.required"));
            }

            this.configuration = configuration;

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

            // disable the caching of the artifacts themselves when they are already available on the local filesystem.
            settings.setDefaultUseOrigin(true);

            this.ivy = Ivy.newInstance(settings);
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Resolves all configurations mentioned in the given descriptor.  Note that this does not actually generate a
     * resolved descriptor - see {@link #deliverDescriptor(IvyModuleDescriptor)} for how to do so.
     *
     * @param descriptor the descriptor to resolve
     * @return Ivy report detailing what was resolve
     * @throws IOException if a descriptor cannot be read/written
     * @throws ParseException if a descriptor cannot be parsed
     */
    public ResolveReport resolveDescriptor(IvyModuleDescriptor descriptor) throws IOException, ParseException
    {
        LOCK.lock();
        try
        {
            return resolve(descriptor.getDescriptor(), descriptor.getDescriptor().getConfigurationsNames());
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Delivers the given descriptor, i.e. generates a resolved version of it (with, for example, dynamic revisions
     * replaced by their resolved counterparts).
     *
     * @param descriptor the descriptor to deliver
     * @return a resolved version of the given descriptor
     * @throws Exception on error
     */
    public IvyModuleDescriptor deliverDescriptor(IvyModuleDescriptor descriptor) throws Exception
    {
        LOCK.lock();
        try
        {
            // Deliver to a local temp file, and reload the descriptor from it.
            File tmp = null;
            try
            {
                tmp = FileSystemUtils.createTempDir();
                File ivyFile = new File(tmp, "ivy.xml");
                ModuleRevisionId mrid = descriptor.getDescriptor().getModuleRevisionId();
                ivy.deliver(mrid, descriptor.getRevision(), ivyFile.getCanonicalPath());
                IvyModuleDescriptor deliveredDescriptor = IvyModuleDescriptor.newInstance(ivyFile, configuration);
                // Hackishly disconnect the descriptor from the temp file.
                ReflectionUtils.setFieldValue(deliveredDescriptor.getDescriptor(), "resource", null);
                return deliveredDescriptor;
            }
            finally
            {
                cleanupTempDir(tmp);
            }
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Publish local artifacts to the ivy repository.  The artifacts to be published are
     * those defined in the module descriptor that match the specified confNames.
     * <p/>
     * The location of the artifact file to be published is defined by the extra attribute
     * 'sourcefile' which needs to be defined for each artifact being published.
     *
     * @param descriptor the descriptor that defines the artifacts to be published.
     * @param stageNames the stage names identifying the set of artifacts to be published.  If
     *                   blank, all artifacts will be published.
     * @throws IOException if there is a failure to publish an artifact.
     */
    public void publishArtifacts(IvyModuleDescriptor descriptor, String... stageNames) throws IOException
    {
        LOCK.lock();
        try
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

                if (stageNames.length > 0)
                {
                    options.setConfs(IvyEncoder.encodeNames(stageNames));
                }

                Collection bad = checkCanPublishArtifacts(descriptor, stageNames);
                if (bad.size() > 0)
                {
                    throw new IOException(I18N.format("failure.publish.missingSourcefile"));
                }

                Collection missing = ivy.getPublishEngine().publish(descriptor.getDescriptor(), Arrays.asList("[" + IvyModuleDescriptor.SOURCEFILE + "]"), dependencyResolver, options);
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
        finally
        {
            LOCK.unlock();
        }
    }

    // sanity check that 'sourcefile' is defined for the artifacts to be published.
    private Collection<Artifact> checkCanPublishArtifacts(IvyModuleDescriptor descriptor, String... stageNames) throws IOException
    {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        if (stageNames.length > 0)
        {
            for (String stageName : stageNames)
            {
                artifacts.addAll(Arrays.asList(descriptor.getArtifacts(stageName)));
            }
        }
        else
        {
            artifacts.addAll(Arrays.asList(descriptor.getAllArtifacts()));
        }

        List<Artifact> badArtifacts = new LinkedList<Artifact>();
        for (Artifact artifact : artifacts)
        {
            if (!artifact.getId().getExtraAttributes().containsKey(IvyModuleDescriptor.SOURCEFILE))
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
    public void publishDescriptor(IvyModuleDescriptor descriptor) throws IOException
    {
        LOCK.lock();

        try
        {
            // we can only publish from a file, so we need to deliver the descriptor to a local file first.
            File tmp = null;
            // annoying but necessary.  See CustomURLHandler for details.
            URLHandler originalDefault = URLHandlerRegistry.getDefault();
            try
            {
                URLHandlerRegistry.setDefault(new CustomURLHandler());

                String revision = descriptor.getRevision();

                DefaultModuleDescriptor moduleDescriptor = descriptor.getDescriptor();

                tmp = FileSystemUtils.createTempDir();
                File ivyFile = new File(tmp, "ivy.xml");
                XmlModuleDescriptorWriter.write(moduleDescriptor, ivyFile);

                ModuleRevisionId mrid = moduleDescriptor.getModuleRevisionId();

                PublishOptions options = new PublishOptions();
                options.setOverwrite(true);
                options.setUpdate(true);
                options.setHaltOnMissing(false); // we don't care about missing artifacts here, just that the ivy file gets published.
                options.setConfs(new String[]{IvyModuleDescriptor.ALL_STAGES});
                options.setSrcIvyPattern(ivyFile.getCanonicalPath());

                if (StringUtils.stringSet(revision))
                {
                    options.setPubrevision(revision);
                }
                ivy.publish(mrid, Collections.emptySet(), RESOLVER_NAME, options);
            }
            finally
            {
                URLHandlerRegistry.setDefault(originalDefault);

                cleanupTempDir(tmp);
            }
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Retrieve the artifacts defined by the dependencies in this descriptor, and write them to the
     * file system according to the target pattern.
     *
     * @param descriptor    the descriptor defining the dependencies to be retrieved.
     * @param stageName     the name of the stage doing the retrieval
     * @param targetPattern the pattern defining where the artifacts will be written.  This pattern,
     *                      supports the usual ivy tokens.
     * @param sync          if true, the destination is synchronised - i.e. any files not retrieved
     *                      will be remove from the destination directory
     * @return a retrieval report containing details of the artifacts that were retrieved.
     * @throws java.io.IOException  on error
     * @throws java.text.ParseException on error
     */
    public IvyRetrievalReport retrieveArtifacts(DefaultModuleDescriptor descriptor, String stageName, String targetPattern, boolean sync) throws IOException, ParseException
    {
        LOCK.lock();
        try
        {
            // annoying but necessary.  See CustomURLHandler for details.
            URLHandler originalDefault = URLHandlerRegistry.getDefault();
            try
            {
                URLHandlerRegistry.setDefault(new CustomURLHandler());

                IvyRetrievalReport report = new IvyRetrievalReport();

                ModuleRevisionId mrid = descriptor.getModuleRevisionId();

                String conf = IvyEncoder.encode(stageName);
                ResolveReport resolveReport = resolve(descriptor, conf);
                IvyModuleDescriptor ivyDescriptor = new IvyModuleDescriptor(descriptor, configuration);
                if (resolveReportHasProblems(resolveReport, report, conf, ivyDescriptor.getOptionalDependencies()))
                {
                    return report;
                }

                RetrieveOptions options = new RetrieveOptions();
                options.setConfs(new String[]{conf});
                options.setSync(sync);

                ivy.retrieve(mrid, targetPattern, options);

                recordDownloadedArtifacts(targetPattern, report, mrid, options);
                decodeArtifactFilenames(targetPattern, report);

                return report;
            }
            finally
            {
                URLHandlerRegistry.setDefault(originalDefault);
            }
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Cleanup any items loaded into the cache used by this client.
     */
    public void cleanup()
    {
        LOCK.lock();
        try
        {
            IvySettings settings = ivy.getSettings();

            ResolutionCacheManager resolutionCacheManager = settings.getResolutionCacheManager();
            resolutionCacheManager.clean();

            RepositoryCacheManager[] caches = settings.getRepositoryCacheManagers();
            for (RepositoryCacheManager cache : caches)
            {
                cache.clean();
            }
        }
        finally
        {
            LOCK.unlock();
        }
    }

    private void decodeArtifactFilenames(String targetPattern, IvyRetrievalReport report)
    {
        for (Artifact decodedArtifact : report.getRetrievedArtifacts())
        {
            // The encoded artifact is what ivy will have been working with.
            Artifact encodedArtifact = IvyEncoder.encode(decodedArtifact);
            String decodedArtifactPath = IvyPatternHelper.substitute(targetPattern, decodedArtifact);
            String encodedArtifactPath = IvyPatternHelper.substitute(targetPattern, encodedArtifact);
            if (!encodedArtifactPath.equals(decodedArtifactPath))
            {
                // The encoded / decoded paths are different, so we need a rename.
                File decodedArtifactFile = new File(decodedArtifactPath);
                File encodedArtifactFile = new File(encodedArtifactPath);
                if (encodedArtifactFile.exists() && !encodedArtifactFile.renameTo(decodedArtifactFile))
                {
                    LOG.warning("Failed to rename artifact from " + encodedArtifactPath + " to " + decodedArtifactPath);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private boolean resolveReportHasProblems(ResolveReport resolveReport, IvyRetrievalReport report, String confName, Set<String> optionalDependencies)
    {
        ConfigurationResolveReport configurationResolveReport = resolveReport.getConfigurationReport(confName);
        if (configurationResolveReport.hasError())
        {
            List<IvyNode> dependencies = resolveReport.getDependencies();
            for (IvyNode dependency : dependencies)
            {
                if (dependency.hasProblem()) // this problem flag seems to be isolated to resolution problems.
                {
                    String errMsg = dependency.getProblemMessage();
                    if (!optionalDependencies.contains(IvyEncoder.decode(dependency.getId().getName())) || !isConfigurationMissingMessage(errMsg))
                    {
                        Artifact artifact = new DefaultArtifact(dependency.getResolvedId(), null, "?", "?", "?");
                        ArtifactDownloadReport artifactDownloadReport = new ArtifactDownloadReport(artifact);
                        artifactDownloadReport.setDownloadStatus(DownloadStatus.FAILED);
                        if (errMsg.length() > 0)
                        {
                            artifactDownloadReport.setDownloadDetails("unresolved dependency: " + dependency.getId() + ": " + errMsg);
                        }
                        else
                        {
                            artifactDownloadReport.setDownloadDetails("unresolved dependency: " + dependency.getId());
                        }
                        report.addDownloadReports(IvyEncoder.decode(artifactDownloadReport));
                    }
                }
                else
                {
                    // Although the dependency didn't have any problems, the download reports may have,
                    // for example if the modules artifacts could not be located.
                    ArtifactDownloadReport[] downloadReports = configurationResolveReport.getDownloadReports(dependency.getResolvedId());
                    if (downloadReports.length > 0)
                    {
                        for (ArtifactDownloadReport downloadReport : downloadReports)
                        {
                            report.addDownloadReports(IvyEncoder.decode(downloadReport));
                        }
                    }
                }
            }
        }

        return report.hasFailures();
    }

    private boolean isConfigurationMissingMessage(String problemMessage)
    {
        // This is a hack, but there is no concept of an optional dependency in
        // Ivy, and this gives us the desired result without either modifying
        // Ivy or replicating a huge chunk of the resolve process ourselves  If
        // this bites us then patching Ivy to support optional dependencies is
        // probably the "nicer" way to achieve the same effect.
        //
        // To reduce the chance of this hack blowing up I have confirmed that
        // Ivy only produces this string in this specific case, and there are
        // relevant test cases.
        return problemMessage.contains("configuration not found in");
    }

    @SuppressWarnings({"unchecked"})
    private void recordDownloadedArtifacts(String targetPattern, IvyRetrievalReport report, ModuleRevisionId mrid, RetrieveOptions options) throws ParseException, IOException
    {
        Map<ArtifactDownloadReport, Set<String>> artifactDownloadReports = ivy.getRetrieveEngine().determineArtifactsToCopy(mrid, targetPattern, options);
        for (ArtifactDownloadReport artifactDownloadReport : artifactDownloadReports.keySet())
        {
            report.addDownloadReports(IvyEncoder.decode(artifactDownloadReport));
        }
    }

    /**
     * Resolve the descriptors dependencies, identifying the artifacts described and
     * caching the result.
     *
     * @param descriptor the descriptor whose dependencies are being resolved.
     * @param confs      the (already encoded) names of the configuration to resolve
     * @return a resolve report.
     *
     * @throws java.io.IOException  on error
     * @throws java.text.ParseException on error
     */
    private ResolveReport resolve(ModuleDescriptor descriptor, String... confs) throws IOException, ParseException
    {
        ResolveOptions options = new ResolveOptions();
        options.setValidate(ivy.getSettings().doValidate());
        options.setConfs(confs);
        options.setCheckIfChanged(true);
        options.setUseCacheOnly(false);
        return ivy.resolve(descriptor, options);
    }

    /**
     * Set this message logger to receive all future logging messages from ivy.
     * This logger will continue to receive messages until it is popped.
     *
     * @param logger the logger to receive logging messages.
     */
    public void pushMessageLogger(MessageLogger logger)
    {
        LOCK.lock();
        try
        {
            ivy.getLoggerEngine().pushLogger(logger);
        }
        finally
        {
            LOCK.unlock();
        }
    }

    /**
     * Enter a new context, allowing interruption of threads within ivy operations.  This method
     * (and the operations themselves) should be called by a thread that is disposable.  When the
     * operations are done you must call {@link #popContext()}.
     */
    public void pushContext()
    {
        ivy.pushContext();
    }

    /**
     * Pops the current context, must be paired with a prior call to {@link #pushContext()}.
     */
    public void popContext()
    {
        ivy.popContext();
    }

    /**
     * Attempts to interrupt a running Ivy operation.  Must be passed a thread that has previously
     * called {@link #pushContext()}.  If the operation does not die quickly enough Ivy tries to
     * forcefully stop the given thread, so make sure it is disposable!
     *
     * @param operatingThread the thread running the ivy operation (in a context!)
     */
    public void interrupt(Thread operatingThread)
    {
        ivy.interrupt(operatingThread);
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

    private void cleanupTempDir(File tmp)
    {
        if (tmp != null)
        {
            try
            {
                rmdir(tmp);
            }
            catch (IOException e)
            {
                LOG.warning(I18N.format("warning.file.cleanup.failure", e.getMessage()));
            }
        }
    }
}
