/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * The ivy configuration defines how the underlying ivy instance will be
 * configured.  Most importantly, it defines the format and location of
 * the artifact repository that will be used by ivy.
 */
public class IvyConfiguration
{
    private static final String DEFAULT_PATTERN_MODULE = "([organisation]/)[module]";
    private static final String DEFAULT_PATTERN_ARTIFACT = DEFAULT_PATTERN_MODULE + "/([stage]/)[artifact](-[revision])(.[ext])";
    private static final String DEFAULT_PATTERN_IVY = DEFAULT_PATTERN_MODULE + "/ivy(-[revision]).xml";

    private static final String IVY_CACHE_DIR = "ivy.cache.dir";
    private static final String IVY_CACHE_RESOLUTION = "ivy.cache.resolution";
    private static final String IVY_CACHE_REPOSITORY = "ivy.cache.repository";
    private static final String IVY_CACHE_ARTIFACT_PATTERN = "([organisation]/)[module]/([stage]/)([type]s/)[artifact]-[revision](.[ext])";

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
     * The pattern used to construct paths to a module directory within the repository.
     */
    private String modulePattern = DEFAULT_PATTERN_MODULE;
    /**
     * The pattern used to construct paths to ivy files within the repository.
     */
    private String ivyPattern = DEFAULT_PATTERN_IVY;

    /**
     * The base path to the repository, in the form of a URI.  This can be on
     * the local or remote file system.
     */
    private String repositoryBase;

    /**
     * The base path for the cache.
     */
    private File cacheBase;

    public IvyConfiguration()
    {
    }

    public IvyConfiguration(File repositoryBase, String modulePattern, String artifactPattern, String ivyPattern)
    {
        this(repositoryBase.toURI().toString());
        this.modulePattern = modulePattern;
        this.artifactPattern = artifactPattern;
        this.ivyPattern = ivyPattern;
    }

    public IvyConfiguration(String repositoryBase)
    {
        this.repositoryBase = repositoryBase;
    }

    /**
     * The module pattern defines the expected layout of module directories within the repository.
     *
     * @return the module pattern
     *
     * @see #getModulePath(org.apache.ivy.core.module.id.ModuleRevisionId) 
     */
    public String getModulePattern()
    {
        return modulePattern;
    }

    /**
     * The artifact pattern defines the expected layout of artifacts within
     * the remote repository.
     *
     * @return the artifact pattern
     *
     * @see #getArtifactPath(org.apache.ivy.core.module.descriptor.Artifact)
     * @see #getArtifactPath(org.apache.ivy.core.module.id.ModuleRevisionId, String, String)
     */
    public String getArtifactPattern()
    {
        return artifactPattern;
    }

    /**
     * The ivy pattern defines the expected layout of ivy files within
     * the remote repository.
     *
     * @return the ivy pattern
     *
     * @see #getIvyPath(org.apache.ivy.core.module.id.ModuleRevisionId)
     * @see #getIvyPath(org.apache.ivy.core.module.id.ModuleRevisionId, String)
     */
    public String getIvyPattern()
    {
        return ivyPattern;
    }

    /**
     * Load the ivy settings.  These settings are a combination of the
     * default ivy settings and any variables that are defined by this
     * configuration instance.
     *
     * @return  the ivy settings instance.
     *
     * @throws IOException if the local ivysettings.xml template
     * can not be located.
     * @throws ParseException if the local ivysettings.xml template
     * is invalid.
     */
    public IvySettings loadSettings() throws IOException, ParseException
    {
        IvySettings settings = new IvySettings();
        settings.load(getClass().getResource("ivysettings.xml"));
        settings.setDefaultCacheArtifactPattern(IVY_CACHE_ARTIFACT_PATTERN);
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

    /**
     * Define the base cache directory to be used for all of ivy's caching.
     * This includes the resolution cache and the artifact cache.
     *
     * @param cacheBase the directory to use as the cache root.
     */
    public void setCacheBase(File cacheBase)
    {
        this.cacheBase = cacheBase;
        setVariable(IVY_CACHE_DIR, cacheBase.toURI().toString());
        String canonicalPath = safeCanonicalPath(cacheBase);
        setVariable(IVY_CACHE_RESOLUTION, canonicalPath);
        setVariable(IVY_CACHE_REPOSITORY, canonicalPath);
    }

    private String safeCanonicalPath(File cacheBase)
    {
        try
        {
            return cacheBase.getCanonicalPath();
        }
        catch (IOException e)
        {
            // We did our best, just use the non-canonical path
            return cacheBase.getAbsolutePath();
        }
    }

    public File getCacheBase()
    {
        return cacheBase;
    }

    public String getRepositoryBase()
    {
        return repositoryBase;
    }

    /**
     * The repository base is the a Uri that identifies the base path
     * used to access the remote artifact repository.
     *
     * @param uri   the repository base uri
     */
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

    /**
     * Get the path of the module directory for the module defined by the module revision id.
     * This path is relative to the base of the repository.
     *
     * @param mrid      the id uniquely identifying the module of interest
     * @return the path relative to the base of the repository
     */
    public String getModulePath(ModuleRevisionId mrid)
    {
        return IvyPatternHelper.substitute(getModulePattern(), mrid);
    }

    /**
     * Get the path of the ivy module descriptor defined by the module revision id.
     * This path is relative to the base of the repository.
     *
     * @param mrid  the id uniquely identifying the ivy module descriptor of interest
     * @return the path relative to the base of the repository.
     */
    public String getIvyPath(ModuleRevisionId mrid)
    {
        return getIvyPath(mrid, mrid.getRevision());
    }

    /**
     * Get the path of the ivy module descriptor defined by the module revision id.
     * This path is relative to the base of the repository.
     *
     * @param mrid      the id uniquely identifying the ivy module descriptor of interest
     * @param revision  the revision of the module descriptor, this overrides the revision
     * (if any) that is specified in the module revision id.
     *
     * @return the path relative to the base of the repository.
     */
    public String getIvyPath(ModuleRevisionId mrid, String revision)
    {
        return IvyPatternHelper.substitute(getIvyPattern(), mrid.getOrganisation(), mrid.getName(), revision, "ivy", "xml", "xml");
    }

    /**
     * Get the path of the artifact defined by the artifact instance.
     * This path is relative to the base of the repository.
     *
     * @param artifact  the artifact instance that provides the necessary details
     * to determine its path within the repository.
     *
     * @return the path relative to the base of the repository.
     */
    public String getArtifactPath(Artifact artifact)
    {
        return IvyPatternHelper.substitute(getArtifactPattern(), artifact);
    }

    /**
     * Get the path of the artifact defined by the artifact instance.
     * This path is relative to the base of the repository.
     *
     * @param mrid          the id uniquely identifying the ivy module descriptor of interest
     * @param artifactName  the name of the artifact
     * @param artifactExt   the extension of the artifact.  (This extension is also used as the
     * artifacts type for pattern resolution purposes)
     * @return the path relative to the base of the repository.
     */
    public String getArtifactPath(ModuleRevisionId mrid, String artifactName, String artifactExt)
    {
        return IvyPatternHelper.substitute(getArtifactPattern(), mrid, artifactName, artifactExt, artifactExt);
    }
}
