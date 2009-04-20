package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.pulse.core.dependency.DependencyManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

import java.util.List;
import java.io.IOException;
import java.text.ParseException;

import org.apache.ivy.core.module.status.Status;
import org.apache.ivy.core.module.status.StatusManager;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.settings.IvyVariableContainer;
import org.apache.ivy.core.settings.IvyVariableContainerImpl;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.util.Message;
import org.apache.ivy.Ivy;
import org.apache.ivy.plugins.resolver.URLResolver;

/**
 * The ivy implementation of the dependency manager interface.
 */
public class IvyManager implements DependencyManager
{
    public static String STATUS_INTEGRATION   = "integration";
    public static String STATUS_MILESTONE     = "milestone";
    public static String STATUS_RELEASE       = "release";

    private static final String RESOLVER_NAME = "pulse";

    private static final String VARIABLE_REPOSITORY_BASE = "repository.base";

    /**
     * The artifact pattern used by the internal pulse repository.
     */
    private static final String PATTERN_ARTIFACT = "${repository.base}/([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";

    /**
     * The ivy pattern used by the internal pulse repository.
     */
    private static final String PATTERN_IVY = "${repository.base}/([organisation]/)[module]/([stage]/)ivy-[revision].xml";

    private IvySettings defaultSettings;

    public void init() throws IOException, ParseException
    {
        // redirect the default logging to our own logging system.
        Message.setDefaultLogger(new IvyMessageLoggerAdapter());

        defaultSettings = loadDefaultSettings();
    }

    private IvySettings loadDefaultSettings() throws ParseException, IOException
    {
        IvySettings settings = new IvySettings();
        settings.load(getClass().getResource("ivysettings.xml"));
        return settings;
    }

    /**
     * Get the list of statuses available for use with builds.
     *
     * @return a list of strings representing valid statuses.
     */
    public List<String> getStatuses()
    {
        @SuppressWarnings("unchecked")
        List<Status> statues = (List<Status>) StatusManager.getCurrent().getStatuses();
        return CollectionUtils.map(statues, new Mapping<Status, String>()
        {
            public String map(Status s)
            {
                return s.getName();
            }
        });
    }

    /**
     * Get the default status.
     *
     * @return the default status.
     */
    public String getDefaultStatus()
    {
        return StatusManager.getCurrent().getDefaultStatus();
    }

    private Ivy newIvyInstance(String repositoryBase) throws IOException, ParseException
    {
        final IvyVariableContainer variables = new IvyVariableContainerImpl();
        variables.setVariable(VARIABLE_REPOSITORY_BASE, repositoryBase, true);

        URLResolver resolver = new URLResolver();
        resolver.setName(RESOLVER_NAME);
        resolver.addArtifactPattern(IvyPatternHelper.substituteVariables(PATTERN_ARTIFACT, variables));
        resolver.addIvyPattern(IvyPatternHelper.substituteVariables(PATTERN_IVY, variables));
        resolver.setCheckmodified(true);

        // can not clone the default settings, so we need to reload them since we are making changes.
        IvySettings settings = loadDefaultSettings();

        settings.addResolver(resolver);
        settings.setDefaultResolver(resolver.getName());

        return Ivy.newInstance(settings);
    }

    /**
     * Get a configured instance of support ivy.
     *
     * @param repositoryBase    defines the base path to the internal pulse repository.  The
     * format of this field must be a valid url.
     * @return  a new configured ivy support instance.
     *
     * @throws Exception on error.
     */
    public IvySupport getIvySupport(String repositoryBase) throws Exception
    {
        return new IvySupport(newIvyInstance(repositoryBase));
    }

    /**
     * Get the default ivy settings.
     *
     * @return the default ivy settings instance.
     */
    public IvySettings getDefaultSettings()
    {
        return defaultSettings;
    }
}
