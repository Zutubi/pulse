package com.zutubi.prototype.config;

import com.zutubi.prototype.type.*;
import com.zutubi.pulse.prototype.config.*;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.setup.SetupConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * Registers the Pulse built-in configuration types.
 */
public class ConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRegistry.class);
    
    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public void init()
    {
        try
        {
            CompositeType setupConfig = typeRegistry.register(SetupConfiguration.class);
            configurationPersistenceManager.register("setup", setupConfig);

            // scm configuration
            CompositeType scmConfig = typeRegistry.register("scmConfig", ScmConfiguration.class);
            typeRegistry.register("svnConfig", SvnConfiguration.class);
            typeRegistry.register("cvsConfig", CvsConfiguration.class);
            typeRegistry.register("perforceConfig", PerforceConfiguration.class);

            // sort out the extensions.
            scmConfig.addExtension("svnConfig");
            scmConfig.addExtension("cvsConfig");
            scmConfig.addExtension("perforceConfig");

            CompositeType typeConfig = typeRegistry.register("typeConfig", ProjectTypeConfiguration.class);
            typeRegistry.register("antConfig", AntTypeConfiguration.class);
            typeRegistry.register("mavenConfig", MavenTypeConfiguration.class);

            typeConfig.addExtension("antConfig");
            typeConfig.addExtension("mavenConfig");

            // cleanup rule configuration
            typeRegistry.register("cleanupRuleConfig", CleanupRuleConfiguration.class);

            // commit message processors.
            CompositeType commitConfig = typeRegistry.register("commitConfig", CommitMessageConfiguration.class);
            typeRegistry.register("jiraCommitConfig", JiraCommitMessageConfiguration.class);
            typeRegistry.register("customCommitConfig", CustomCommitMessageConfiguration.class);

            commitConfig.addExtension("jiraCommitConfig");
            commitConfig.addExtension("customCommitConfig");

            // change view configuration
            CompositeType changeViewerConfig = typeRegistry.register("changeViewerConfig", ChangeViewerConfiguration.class);
            typeRegistry.register("fisheyeChangeViewerConfig", FisheyeConfiguration.class);
            typeRegistry.register("customChangeViewerConfig", CustomChangeViewerConfiguration.class);

            changeViewerConfig.addExtension("fisheyeChangeViewerConfig");
            changeViewerConfig.addExtension("customChangeViewerConfig");

            CompositeType artifactConfig = typeRegistry.register("artifactConfig", ArtifactConfiguration.class);
            typeRegistry.register("fileArtifactConfig", FileArtifactConfiguration.class);
            typeRegistry.register("directoryArtifactConfig", DirectoryArtifactConfiguration.class);

            artifactConfig.addExtension("fileArtifactConfig");
            artifactConfig.addExtension("directoryArtifactConfig");

            // generated dynamically as new components are registered.
            CompositeType projectConfig = typeRegistry.register("projectConfig", ProjectConfiguration.class);
            projectConfig.addProperty(new TypeProperty("scm", typeRegistry.getType("scmConfig")));
            projectConfig.addProperty(new TypeProperty("type", typeRegistry.getType("typeConfig")));
            projectConfig.addProperty(new TypeProperty("cleanup", typeRegistry.getType("cleanupRuleConfig")));
            projectConfig.addProperty(new TypeProperty("changeViewer", typeRegistry.getType("changeViewerConfig")));

            // Triggers
            CompositeType triggerConfig = typeRegistry.register("triggerConfig", TriggerConfiguration.class);
            typeRegistry.register("buildCompletedConfig", BuildCompletedTriggerConfiguration.class);
            triggerConfig.addExtension("buildCompletedConfig");
            MapType triggers = new MapType(configurationPersistenceManager);
            triggers.setTypeRegistry(typeRegistry);
            triggers.setCollectionType(typeRegistry.getType("triggerConfig"));
            projectConfig.addProperty(new TypeProperty("trigger", triggers));

            ListType artifacts = new ListType(configurationPersistenceManager);
            artifacts.setTypeRegistry(typeRegistry);
            artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
            projectConfig.addProperty(new TypeProperty("artifact", artifacts));

            MapType commitTransformers = new MapType(configurationPersistenceManager);
            commitTransformers.setTypeRegistry(typeRegistry);
            commitTransformers.setCollectionType(typeRegistry.getType("commitConfig"));
            projectConfig.addProperty(new TypeProperty("commit", commitTransformers));

            // define the root level scope.
            ProjectMapType projectCollection = new ProjectMapType(configurationPersistenceManager);
            projectCollection.setTypeRegistry(typeRegistry);
            projectCollection.setCollectionType(projectConfig);

            configurationPersistenceManager.register("project", projectCollection);

            CompositeType globalConfig = typeRegistry.register("globalConfig", GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public GlobalConfiguration getGlobalConfiguration()
    {
        return configurationPersistenceManager.getInstance(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
