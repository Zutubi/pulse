package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.AbstractPatternsBasedResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.apache.ivy.plugins.resolver.URLResolver;

import java.net.URI;
import java.util.Map;
import java.io.File;

/**
 * A ivy client factory implementation that creates an ivy instance configured
 * with a single resolver.  The base path to this resolver is configured via
 * the repository.base variable, passed through to the factory during the
 * create method.  The value of the repository.base variable needs to be a
 * valid URI.
 */
public class DefaultIvyClientFactory
{
    /**
     * The artifact pattern used by the internal pulse repository.
     */
    private static final String PATTERN_ARTIFACT = "([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";

    /**
     * The ivy pattern used by the internal pulse repository.
     */
    private static final String PATTERN_IVY = "([organisation]/)[module]/ivy-[revision].xml";

    public static final String VARIABLE_REPOSITORY_BASE = "repository.base";

    private static final String RESOLVER_NAME = "pulse";

    private String artifactPattern;

    private String ivyPattern;

    private IvyManager ivyManager;

    public DefaultIvyClientFactory()
    {
        this(PATTERN_ARTIFACT, PATTERN_IVY);
    }

    public DefaultIvyClientFactory(String artifactPattern, String ivyPattern)
    {
        this.artifactPattern = artifactPattern;
        this.ivyPattern = ivyPattern;
    }

    public IvyClient createClient(Map<String, String> variables) throws Exception
    {
        IvySettings settings = ivyManager.loadDefaultSettings();
        for (String variableName : variables.keySet())
        {
            settings.setVariable(variableName, variables.get(variableName));
        }

        String repositoryBase = variables.get(VARIABLE_REPOSITORY_BASE);

        AbstractPatternsBasedResolver resolver;
        URI uri = new URI(repositoryBase);
        if (isFile(uri))
        {
            File f = new File(uri);
            variables.put(VARIABLE_REPOSITORY_BASE, f.getCanonicalPath());
            resolver = new FileSystemResolver();
        }
        else
        {
            resolver = new URLResolver();
        }

        resolver.setName(RESOLVER_NAME);
        resolver.addArtifactPattern(IvyPatternHelper.substituteVariables("${"+VARIABLE_REPOSITORY_BASE+"}/" + artifactPattern, variables));
        resolver.addIvyPattern(IvyPatternHelper.substituteVariables("${"+VARIABLE_REPOSITORY_BASE+"}/" + ivyPattern, variables));
        resolver.setCheckmodified(true);

        settings.addResolver(resolver);
        settings.setDefaultResolver(RESOLVER_NAME);

        return new IvyClient(Ivy.newInstance(settings), ivyPattern, artifactPattern);
    }

    private boolean isFile(URI uri)
    {
        return uri.getScheme().equals("file");
    }

    public void setIvyManager(IvyManager ivyManager)
    {
        this.ivyManager = ivyManager;
    }
}
