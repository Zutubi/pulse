package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.deliver.DeliverOptions;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.resolve.ResolveEngine;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.util.MessageLogger;
import org.apache.ivy.util.url.URLHandler;
import org.apache.ivy.util.url.URLHandlerRegistry;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The IvySupport provides a facade around the ivy dependency management system
 */
public class IvySupport
{
    private static final Logger LOG = Logger.getLogger(IvySupport.class);

    private static final String[] ALL_CONFS = new String[]{"*"};

    public static final String CONFIGURATION_BUILD = "build";

    private Ivy ivy;

    public IvySupport(Ivy ivy)
    {
        this.ivy = ivy;
    }

    /**
     * Deliver the resolved descriptor of the specified module to the default resolver.  This default
     * repository needs to use a local file system path for its ivy pattern for this to work.
     *
     * @param mrid     the id of the module descriptor to be delivered
     * @param revision the revision of the delivered file.
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

    public void resolve(ModuleDescriptor descriptor) throws IOException, ParseException
    {
        // we need to resolve the ivy.xml files before we do anything that uses them.
        ResolveOptions options = new ResolveOptions();
        options.setValidate(ivy.getSettings().doValidate());
        options.setConfs(ALL_CONFS);
        options.setCheckIfChanged(true);

        ivy.resolve(descriptor, options);
    }

    public boolean isResolved(ModuleRevisionId mrid)
    {
        File ivyFile = getCache().getResolvedIvyFileInCache(mrid);
        return ivyFile != null && ivyFile.isFile();
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
