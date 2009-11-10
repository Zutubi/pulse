package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

/**
 * The ivy module descriptor provides a wrapper around ivy's ModuleDescriptor and
 * provides some utility methods to simplify the common types of interactions.
 */
public class IvyModuleDescriptor
{
    private DefaultModuleDescriptor descriptor;
    private IvyConfiguration configuration;

    private static final String CONFIGURATION_BUILD = "build";
    private static final String NAMESPACE_EXTRA_ATTRIBUTES = "e";

    public static final String EXTRA_ATTRIBUTE_STAGE = NAMESPACE_EXTRA_ATTRIBUTES + ":stage";
    public static final String EXTRA_ATTRIBUTE_SOURCE_FILE = NAMESPACE_EXTRA_ATTRIBUTES + ":sourcefile";
    public static final String EXTRA_INFO_BUILD_NUMBER = NAMESPACE_EXTRA_ATTRIBUTES + ":buildNumber";

    public static final long UNKNOWN_BUILD_NUMBER = -1;


    public static IvyModuleDescriptor newInstance(File file, IvyConfiguration configuration) throws Exception
    {
        return newInstance(file.toURI().toURL(), configuration);
    }

    /**
     * Load the module descriptor from the file.
     *
     * @param url               the url refering to an xml formatted module descriptor.
     * @param configuration     the ivy configuration
     *
     * @return  a new instance of the IvyModuleDescriptor
     *
     * @throws Exception is thrown on error.
     */
    public static IvyModuleDescriptor newInstance(URL url, IvyConfiguration configuration) throws Exception
    {
        ModuleDescriptorParser parser =  XmlModuleDescriptorParser.getInstance();

        IvySettings ivySettings = configuration.loadDefaultSettings();

        return new IvyModuleDescriptor((DefaultModuleDescriptor) parser.parseDescriptor(ivySettings, url, ivySettings.doValidate()), configuration);
    }

    public IvyModuleDescriptor(DefaultModuleDescriptor descriptor, IvyConfiguration configuration)
    {
        this.descriptor = descriptor;
        this.configuration = configuration;
    }

    public IvyModuleDescriptor(String org, String module, String revision, IvyConfiguration configuration)
    {
        this(ModuleRevisionId.newInstance(org, module, revision), configuration);
    }

    public IvyModuleDescriptor(String org, String module, String revision, String status, IvyConfiguration configuration)
    {
        this(ModuleRevisionId.newInstance(org, module, revision), status, configuration);
    }

    public IvyModuleDescriptor(ModuleRevisionId mrid, IvyConfiguration configuration)
    {
        this(mrid, IvyStatus.STATUS_INTEGRATION, configuration);
    }

    public IvyModuleDescriptor(ModuleRevisionId mrid, String status, IvyConfiguration configuration)
    {
        this.descriptor = new DefaultModuleDescriptor(mrid, status, null);
        this.descriptor.addExtraAttributeNamespace(NAMESPACE_EXTRA_ATTRIBUTES, "http://ant.apache.org/ivy/extra");
        this.configuration = configuration;
    }

    /**
     * Add an artifact to this module descriptor.
     *
     * @param artifactFile      the file representing the artifact to be added to this descriptor. The name
     * and extension of the artifact are taken from the respective portions of the file.
     * @param confName          the configuration name to which this artifact belongs.
     */
    public void addArtifact(File artifactFile, String confName) throws IOException
    {
        Map<String, String> extraAttributes = new HashMap<String, String>();
        extraAttributes.put(EXTRA_ATTRIBUTE_SOURCE_FILE, artifactFile.getCanonicalPath());
        
        String artifactName = artifactFile.getName().substring(0, artifactFile.getName().lastIndexOf('.'));
        String artifactExt = artifactFile.getName().substring(artifactFile.getName().lastIndexOf('.') + 1);

        addArtifact(artifactName, artifactExt, confName, extraAttributes);
    }

    /**
     * Add an artifact to this module descriptor.
     *
     * @param artifactName      the name of the artifact.
     * @param artifactExt       the extension of the artifact
     * @param confName          the configuration name to which this artifact belongs.
     * @param extraAttributes   any extra attributes to be associated with this artifact.
     */
    public void addArtifact(String artifactName, String artifactExt, String confName, Map<String, String> extraAttributes)
    {
        ensureConfigurationExists(confName);

        MDArtifact ivyArtifact = new MDArtifact(descriptor, artifactName, artifactExt, artifactExt, null, extraAttributes);
        ivyArtifact.addConfiguration(confName);

        descriptor.addArtifact(confName, ivyArtifact);
    }

    /**
     * Add a new dependency to this module descriptor.  The transitive setting
     * of this dependency is false.
     *
     * @param mrid      the module revision id defining the dependency.
     * @param confNames the remote module configuration name mappings.  If not defined, '*' is used.
     */
    public void addDependency(ModuleRevisionId mrid, String... confNames)
    {
        addDependency(mrid, false, confNames);
    }

    /**
     * Add a new dependency to this module descriptor.
     *
     * @param mrid          the module revision id defining the dependency
     * @param transitive    indicating whether or not the dependency is transitive
     * @param confNames     the remote module configuration name mappings. If not defined, '*' will be used.
     */
    public void addDependency(ModuleRevisionId mrid, boolean transitive, String... confNames)
    {
        String masterConf = CONFIGURATION_BUILD;

        ensureConfigurationExists(masterConf);

        String dependencyConf = (confNames.length > 0) ? StringUtils.join(",", confNames) : "*";
        DefaultDependencyDescriptor dependencyDescriptor = new DefaultDependencyDescriptor(descriptor, mrid, true, false, transitive);
        dependencyDescriptor.addDependencyConfiguration(masterConf, dependencyConf);

        descriptor.addDependency(dependencyDescriptor);
    }

    /**
     * Get the build number stored with this module descriptor, or {@link #UNKNOWN_BUILD_NUMBER} if
     * no build number is available.
     *
     * @return  the build number, or {@link #UNKNOWN_BUILD_NUMBER} is non is available.
     *
     * @see com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor#UNKNOWN_BUILD_NUMBER
     */
    public long getBuildNumber()
    {
        Map extraInfo = descriptor.getExtraInfo();
        if (extraInfo.containsKey(EXTRA_INFO_BUILD_NUMBER))
        {
            return Long.valueOf((String)extraInfo.get(EXTRA_INFO_BUILD_NUMBER));
        }
        return UNKNOWN_BUILD_NUMBER;
    }

    public void setBuildNumber(long buildNumber)
    {
        IvyModuleDescriptor.setBuildNumber(descriptor, buildNumber);
    }

    public static void setBuildNumber(ModuleDescriptor descriptor, long buildNumber)
    {
        descriptor.getExtraInfo().put(EXTRA_INFO_BUILD_NUMBER, String.valueOf(buildNumber));
    }

    public String getRevision()
    {
        return descriptor.getRevision();
    }

    public String getStatus()
    {
        return descriptor.getStatus();
    }

    /**
     * Get the path (relative to the repository base) of this module descriptor.
     * 
     * @return  the repository path of this ivy descriptor
     */
    public String getPath()
    {
        return configuration.getIvyPath(descriptor.getModuleRevisionId());
    }

    /**
     * Returns true if the module descriptor has any configured dependencies.
     *
     * @return  true if the module descriptor has dependencies, false otherwise.
     */
    public boolean hasDependencies()
    {
        return descriptor != null && descriptor.getDependencies().length > 0;
    }

    /**
     * Returns a list of paths for all of the artifacts defined by this module descriptor.
     * These paths are relative to the repository base.
     *
     * @return  a list of paths
     */
    public List<String> getArtifactPaths()
    {
        return CollectionUtils.map(descriptor.getAllArtifacts(), new Mapping<Artifact, String>()
        {
            public String map(Artifact artifact)
            {
                return IvyPatternHelper.substitute(configuration.getArtifactPattern(), artifact);
            }
        });
    }

    /**
     * Get the underlying ivy module descriptor.
     *
     * @return underlying ivy module descriptor
     */
    public DefaultModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    private void ensureConfigurationExists(String confName)
    {
        if (descriptor.getConfiguration(confName) == null)
        {
            descriptor.addConfiguration(new Configuration(confName));
        }
    }
}
