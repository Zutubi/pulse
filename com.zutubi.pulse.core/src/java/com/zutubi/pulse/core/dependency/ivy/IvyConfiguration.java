package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.File;
import java.text.ParseException;

/**
 * The ivy configuration defines how the underlying ivy instance will be
 * configured.  Most importantly, it defines the format and location of
 * the artifact repository that will be used by ivy.
 */
public class IvyConfiguration
{
    private static final String DEFAULT_PATTERN_ARTIFACT = "([organisation]/)[module]/([stage]/)[type]s/[artifact]-[revision].[ext]";
    private static final String DEFAULT_PATTERN_IVY = "([organisation]/)[module]/ivy-[revision].xml";
    
    private static final String IVY_CACHE_DIR = "ivy.cache.dir";
    private static final String IVY_CACHE_RESOLUTION = "ivy.cache.resolution";
    private static final String IVY_CACHE_REPOSITORY = "ivy.cache.repository";

    private static final String DEFAULT_RESOLVER_NAME = "pulse";

    /**
     * A set of key value pairs that are passed through to the underlying ivy
     * system.
     */
    public Map<String, String> variables = new HashMap<String, String>();

    /**
     * The pattern used to construct paths to artifacts within the repository.
     */
    private String artifactPattern = DEFAULT_PATTERN_ARTIFACT;
    /**
     * The pattern used to construct paths to ivy files within the repository.
     */
    private String ivyPattern = DEFAULT_PATTERN_IVY;

    /**
     * The base path to the repository, in the form of a URI.  This can be on
     * the local or remote file system.
     */
    private String repositoryBase;

    public IvyConfiguration()
    {
    }

    public IvyConfiguration(String repositoryBase)
    {
        this.repositoryBase = repositoryBase;
    }

    public String getArtifactPattern()
    {
        return artifactPattern;
    }

    public String getIvyPattern()
    {
        return ivyPattern;
    }

    public IvySettings loadDefaultSettings() throws IOException, ParseException
    {
        IvySettings settings = new IvySettings();
        settings.load(getClass().getResource("ivysettings.xml"));

        for (String variableName : getVariables().keySet())
        {
            settings.setVariable(variableName, getVariable(variableName));
        }

        return settings;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }

    public String getVariable(String name)
    {
        return variables.get(name);
    }

    public void setVariable(String name, String value)
    {
        variables.put(name, value);
    }

    public void setCacheBase(File cacheBase) throws IOException
    {
        setVariable(IVY_CACHE_DIR, cacheBase.toURI().toString());
        setVariable(IVY_CACHE_RESOLUTION, cacheBase.getCanonicalPath());
        setVariable(IVY_CACHE_REPOSITORY, cacheBase.getCanonicalPath());
    }

    public String getRepositoryBase()
    {
        return repositoryBase;
    }

    public void setRepositoryBase(String uri)
    {
        checkValidUri(uri);
        repositoryBase = uri;
    }

    private void checkValidUri(String uri)
    {
        try
        {
            new URI(uri);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public String getResolverName()
    {
        return DEFAULT_RESOLVER_NAME;
    }

    public String getIvyPath(ModuleRevisionId mrid)
    {
        return getIvyPath(mrid, mrid.getRevision());
    }

    public String getIvyPath(ModuleRevisionId mrid, String revision)
    {
        return IvyPatternHelper.substitute(getIvyPattern(), mrid.getOrganisation(), mrid.getName(), revision, "ivy", "xml", "xml");
    }

    public String getArtifactPath(Artifact artifact)
    {
        return IvyPatternHelper.substitute(getArtifactPattern(), artifact);
    }

    public String getArtifactPath(ModuleRevisionId mrid, String artifactName, String artifactExt)
    {
        return IvyPatternHelper.substitute(getArtifactPattern(), mrid, artifactName, artifactExt, artifactExt);
    }
}
