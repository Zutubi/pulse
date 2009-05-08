package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.util.*;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.deliver.DeliverOptions;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.resolve.ResolveEngine;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.file.FileResource;
import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.RepositoryResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.MessageLogger;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;
import org.apache.ivy.util.url.CredentialsStore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * The IvyClient provides a facade around the ivy dependency management system
 */
public class IvyClient
{
    private static final Logger LOG = Logger.getLogger(IvyClient.class);

    private static final String[] ALL_CONFS = new String[]{"*"};

    public static final String CONFIGURATION_BUILD = "build";

    private Ivy ivy;
    private String ivyPattern;
    private String artifactPattern;

    public IvyClient(Ivy ivy, String ivyPattern, String artifactPattern)
    {
        this.ivy = ivy;
        this.ivyPattern = ivyPattern;
        this.artifactPattern = artifactPattern;
    }

    /**
     * Deliver the resolved descriptor of the specified module to the default resolver.  This default
     * repository needs to use a local file system path for its ivy pattern for this to work.
     *
     * @param mrid     the id of the module descriptor to be delivered
     * @param revision the revision of the delivered file.
     * 
     * @throws java.io.IOException      on error
     * @throws java.text.ParseException on error
     */
    public void deliver(ModuleRevisionId mrid, String revision) throws IOException, ParseException
    {
        AbstractPatternsBasedResolver resolver = (AbstractPatternsBasedResolver) getArtifactRepositoryResolver();
        String destIvyPattern = (String) resolver.getIvyPatterns().get(0);

        deliver(mrid, revision, destIvyPattern);
    }

    /**
     * Deliver the resolved descriptor of the specified module to the path defined by the destIvyPattern
     * parameter.
     *
     * @param mrid           the id of the resolved module descriptor to be delivered.
     * @param revision       the revision of the delivered descriptor
     * @param destIvyPattern the pattern to which the descriptor will be delivered.
     * @throws IOException    is thrown on error
     * @throws ParseException is thrown on error
     */
    public void deliver(ModuleRevisionId mrid, String revision, String destIvyPattern) throws IOException, ParseException
    {
        //IMPLEMENTATION NOTE:  Delivery only works for the local file system.

        DeliverOptions options = DeliverOptions.newInstance(ivy.getSettings());

        ivy.deliver(mrid, revision, destIvyPattern, options);
    }

    /**
     * Retrieve the dependencies defined by the specified resolved module descriptor, placing them on the local
     * file system as is defined by the target pattern parameter.  The module descriptors 'build' configuration
     * is used for this retrieval.
     *
     * @param mrid          the id of the resolved descriptor for which the dependencies are being retrieved.
     * @param targetPattern the pattern defining where the dependencies will be placed.
     * @throws IOException is thrown on error
     */
    public void retrieve(ModuleRevisionId mrid, String targetPattern) throws IOException
    {
        retrieve(mrid, targetPattern, CONFIGURATION_BUILD);
    }

    /**
     * Retrieve the dependencies defined by the specified 'resolved' module descriptor, using the specified
     * configurations to identify which dependencies to retrieve.  The retrieved artifacts are written to the
     * file system using the specified target pattern.
     *
     * @param mrid          the id of the resolved descriptor for which the dependencies are being retrieved.
     * @param targetPattern the pattern defining where the dependencies will be placed.
     * @param confs         the configurations identifying the artifacts to be retrieved.
     * @throws IOException is thrown on error
     */
    public void retrieve(ModuleRevisionId mrid, String targetPattern, String... confs) throws IOException
    {
        RetrieveOptions options = new RetrieveOptions();
        options.setConfs(confs);

        ivy.retrieve(mrid, targetPattern, options);
    }

    /**
     * Publish artifacts to the default repository resolver.  The artifacts are picked up from the local
     * file system using the defined artifact patterns.
     *
     * @param mrid             the module revision id identifying the module the artifacts are associated with.
     * @param revision         the revision of the artifact being published.
     * @param conf             the configuration defining the artifacts being published.  This is typically the
     *                         name of the stage from which the artifacts are being published.
     * @param artifactPatterns the artifact patterns that define the location of the artifacts on disk.
     * @throws IOException is throw on error
     */
    public void publish(ModuleRevisionId mrid, String revision, String conf, String... artifactPatterns) throws IOException
    {
        Collection srcArtifactsPatterns = Arrays.asList(artifactPatterns);

        PublishOptions options = new PublishOptions();
        options.setOverwrite(true);
        options.setUpdate(true);
        options.setHaltOnMissing(true);
        options.setConfs(new String[]{conf});

        if (TextUtils.stringSet(revision))
        {
            options.setPubrevision(revision);
        }

        String resolverName = getArtifactRepositoryResolver().getName();

        // annoying but necessary.  See CustomURLHandler for details.
        URLHandler originalDefault = URLHandlerRegistry.getDefault();
        try
        {
            URLHandlerRegistry.setDefault(new CustomURLHandler());
            ivy.publish(mrid, srcArtifactsPatterns, resolverName, options);
        }
        finally
        {
            URLHandlerRegistry.setDefault(originalDefault);
        }
    }

    /**
     * Similar to the regular publish, except that special allowances are made for the fact that the artifact
     * being published is an ivy file.
     *
     * @param descriptor the descriptor to be published as an ivy file to the repository
     * @param revision   the revision of the published descriptor.
     * @return a collection of artifacts that are missing.
     * @throws IOException    is thrown on error
     * @throws ParseException is thrown on error
     */
    public Collection publishIvy(ModuleDescriptor descriptor, String revision) throws IOException, ParseException
    {
        // we can only publish from a file, so we need to deliver the descriptor to a local file first.
        File tmp = null;
        try
        {
            tmp = FileSystemUtils.createTempDir();
            String destIvyPattern = tmp.getCanonicalPath() + "/[artifact].[ext]";
            ivy.deliver(descriptor.getModuleRevisionId(), revision, destIvyPattern);

            ModuleRevisionId mrid = descriptor.getModuleRevisionId();

            PublishOptions options = new PublishOptions();
            options.setOverwrite(true);
            options.setUpdate(true);
            options.setHaltOnMissing(false); // we dont care about missing artifacts here, just that the ivy file gets published.
            options.setConfs(new String[]{"*"});
            options.setSrcIvyPattern(new File(tmp, "ivy.xml").getCanonicalPath());

            if (TextUtils.stringSet(revision))
            {
                options.setPubrevision(revision);
            }

            String resolverName = getArtifactRepositoryResolver().getName();

            // annoying but necessary.  See CustomURLHandler for details.
            URLHandler originalDefault = URLHandlerRegistry.getDefault();
            try
            {
                URLHandlerRegistry.setDefault(new CustomURLHandler());
                return ivy.publish(mrid, Collections.emptySet(), resolverName, options);
            }
            finally
            {
                URLHandlerRegistry.setDefault(originalDefault);
            }
        }
        finally
        {
            if (tmp != null && !FileSystemUtils.rmdir(tmp))
            {
                LOG.warning("Failed to clean up: " + tmp.getCanonicalPath());
            }
        }
    }

    public ResolveReport resolve(ModuleDescriptor descriptor) throws IOException, ParseException
    {
        ResolveOptions options = new ResolveOptions();
        options.setValidate(ivy.getSettings().doValidate());
        options.setConfs(ALL_CONFS);
        options.setCheckIfChanged(true);

        return ivy.resolve(descriptor, options);
    }

    public ResolveReport resolve(ModuleRevisionId mrid, String... confs) throws IOException, ParseException
    {
        ResolveOptions options = new ResolveOptions();
        options.setValidate(ivy.getSettings().doValidate());
        options.setConfs(ALL_CONFS);
        options.setUseCacheOnly(true);
        options.setConfs(confs);
        options.setResolveId(ResolveOptions.getDefaultResolveId(mrid.getModuleId()));

        return ivy.resolve(mrid, options, false);
    }

    public List<String> getArtifactPaths(ModuleRevisionId mrid) throws IOException, ParseException
    {
        final RepositoryResolver resolver = (RepositoryResolver) ivy.getSettings().getDefaultResolver();

        ResolveOptions resolveOptions = new ResolveOptions();
        ResolveData resolveData = new ResolveData(ivy.getResolveEngine(), resolveOptions);
        DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(mrid, true);

        ResolvedResource resolvedResource = resolver.findIvyFileRef(dependencyDescriptor, resolveData);
        if (resolvedResource == null || !resolvedResource.getResource().exists())
        {
            // failed to locate the ivy file, we can not continue.
            throw new IllegalArgumentException("Failed to located ivy descriptor for '" + mrid + "'");
        }

        XmlModuleDescriptorParser parser = XmlModuleDescriptorParser.getInstance();
        Resource resource = resolvedResource.getResource();
        URL ivyFileUrl = resourceToURL(resource);
        ModuleDescriptor repositoryBasedDescriptor = parser.parseDescriptor(ivy.getSettings(), ivyFileUrl, false);

        List<ArtifactOrigin> artifactOrigins = CollectionUtils.map(repositoryBasedDescriptor.getAllArtifacts(), new Mapping<Artifact, ArtifactOrigin>()
        {
            public ArtifactOrigin map(Artifact artifact)
            {
                return resolver.locate(artifact);
            }
        });

        // resolver.locate returns null if the artifact does not exist, so we filter out the nulls.
        List<ArtifactOrigin> artifacts = CollectionUtils.filter(artifactOrigins, new Predicate<ArtifactOrigin>()
        {
            public boolean satisfied(ArtifactOrigin artifactOrigin)
            {
                return artifactOrigin != null;
            }
        });

        return CollectionUtils.map(artifacts, new Mapping<ArtifactOrigin, String>()
        {
            public String map(ArtifactOrigin artifact)
            {
                return IvyPatternHelper.substitute(artifactPattern, artifact.getArtifact(), artifact);
            }
        });
    }

    private URL resourceToURL(Resource resource) throws MalformedURLException
    {
        if (resource instanceof FileResource)
        {
            return new File(resource.getName()).toURI().toURL();
        }
        else
        {
            return new URL(resource.getName());
        }
    }

    public String getIvyPath(ModuleRevisionId mrid) throws IOException, ParseException
    {
        return IvyPatternHelper.substitute(ivyPattern, mrid);
    }

    public boolean isResolved(ModuleRevisionId mrid)
    {
        File ivyFile = getCache().getResolvedIvyFileInCache(mrid);
        return ivyFile != null && ivyFile.isFile();
    }

    public void addCredentials(String host, String user, String pass)
    {
        CredentialsStore.INSTANCE.addCredentials("Pulse", host, user, pass);
    }

    public IvySettings getSettings()
    {
        return ivy.getSettings();
    }

    public ResolveEngine getResolveEngine()
    {
        return ivy.getResolveEngine();
    }

    public void setMessageLogger(MessageLogger logger)
    {
        ivy.getLoggerEngine().pushLogger(logger);
    }

    private ResolutionCacheManager getCache()
    {
        return ivy.getResolutionCacheManager();
    }

    public DependencyResolver getArtifactRepositoryResolver()
    {
        return ivy.getSettings().getDefaultResolver();
    }

    /**
     * Get a command that wraps the dependency retrieve process.
     *
     * @return a command instance able to run a dependency retrieve during a
     *         recipe execution.
     */
    public CommandConfiguration getRetrieveCommand()
    {
        // Note: the creation of this command is being embedded in teh ivy support
        // to allow the recipe processor tests to 'override' the retrieve command
        // with a noop.
        RetrieveDependenciesCommandConfiguration command = new RetrieveDependenciesCommandConfiguration();
        command.setName("retrieve");
        command.setIvy(this);
        return command;
    }

    /**
     * Get a command that wraps the artifact publish process.
     *
     * @param request the recipe request during which this publish is
     *                being run.
     * @return a command instance able to run a publish at the end of a
     *         recipe execution.
     */
    public CommandConfiguration getPublishCommand(RecipeRequest request)
    {
        // Note: the creation of this command is being embedded in teh ivy support
        // to allow the recipe processor tests to 'override' the publish command
        // with a noop.
        PublishArtifactsCommandConfiguration command = new PublishArtifactsCommandConfiguration();
        command.setName("publish");
        command.setRequest(request);
        command.setIvy(this);
        return command;
    }

    /**
     * Return true if the specified descriptor has configured dependencies.
     * @param descriptor    the descriptor in question
     * @return  true if the descriptor has dependencies, false otherwise.
     */
    public boolean hasDependencies(ModuleDescriptor descriptor)
    {
        return descriptor != null && descriptor.getDependencies().length > 0;
    }

    /**
     * Return true if the specified descriptor has artifacts for the specified
     * configuration
     *
     * @param descriptor    the descriptor in question
     * @param conf          the configuration
     * @return  true if the descriptor has artifacts for the specified configuration.
     */
    public boolean hasArtifacts(ModuleDescriptor descriptor, String conf)
    {
        return descriptor != null && descriptor.getArtifacts(conf).length > 0;
    }
}
