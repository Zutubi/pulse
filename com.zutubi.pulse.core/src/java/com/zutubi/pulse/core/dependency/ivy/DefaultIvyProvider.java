package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.NullaryFunction;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.settings.IvyVariableContainer;
import org.apache.ivy.core.settings.IvyVariableContainerImpl;
import org.apache.ivy.plugins.resolver.URLResolver;

/**
 * The default implementation of the IvyProvider interface.  This provider
 * uses the ivysettings.xml file as the basis for the IvySettings configuration,
 * and dynamically adds a URL Repository resolver for Pulses' internal artifact
 * repository.
 */
public class DefaultIvyProvider implements IvyProvider
{
    private static final String VARIABLE_REPOSITORY_BASE = "repository.base";

    /**
     * The artifact pattern used by the internal pulse repository.
     */
    private static final String PATTERN_ARTIFACT = "${repository.base}/([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";

    /**
     * The ivy pattern used by the internal pulse repository.
     */
    private static final String PATTERN_IVY = "${repository.base}/([organisation]/)[module]/([stage]/)ivy-[revision].xml";


    public IvySupport getIvySupport(String repositoryBase) throws Exception
    {
        final IvyVariableContainer variables = new IvyVariableContainerImpl();
        variables.setVariable(VARIABLE_REPOSITORY_BASE, repositoryBase, true);

        // Load the ivy settings, disabling the default ivy logging whilst doing so.
        IvySettings settings = IvyLogUtils.runQuietly(new NullaryFunction<IvySettings>()
        {
            public IvySettings process()
            {
                try
                {
                    IvySettings settings = new IvySettings(variables);
                    settings.load(getClass().getResource("ivysettings.xml"));
                    return settings;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

/*
        This xml is the equivalent of the programmatically configured resolver. 
        <settings defaultResolver="pulse"/>
        <resolvers>
            <url name="pulse" checkmodified="true">
                <ivy pattern="${repository.base}/${repository.ivy.pattern}"/>
                <artifact pattern="${repository.base}/${repository.artifact.pattern}"/>
            </url>
        </resolvers>
*/

        URLResolver urlResolver = new URLResolver();
        urlResolver.setName("pulse");
        urlResolver.addArtifactPattern(IvyPatternHelper.substituteVariables(PATTERN_ARTIFACT, variables));
        urlResolver.addIvyPattern(IvyPatternHelper.substituteVariables(PATTERN_IVY, variables));
        urlResolver.setCheckmodified(true);

        settings.addResolver(urlResolver);
        settings.setDefaultResolver(urlResolver.getName());

        return new IvySupport(Ivy.newInstance(settings));
    }
}
