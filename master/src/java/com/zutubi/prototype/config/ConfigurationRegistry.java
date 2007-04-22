package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.type.*;
import com.zutubi.pulse.prototype.config.*;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.setup.SetupConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers the Pulse built-in configuration types.
 */
public class ConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRegistry.class);

    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private Map<CompositeType, CompositeType> checkTypeMapping = new HashMap<CompositeType, CompositeType>();

    public void init()
    {
        try
        {
            CompositeType setupConfig = registerConfigurationType(SetupConfiguration.class);
            configurationPersistenceManager.register("setup", setupConfig);

            CompositeType typeConfig = registerConfigurationType("typeConfig", ProjectTypeConfiguration.class);
            registerConfigurationType("antConfig", AntTypeConfiguration.class);
            registerConfigurationType("mavenConfig", MavenTypeConfiguration.class);

            typeConfig.addExtension("antConfig");
            typeConfig.addExtension("mavenConfig");

            // cleanup rule configuration
            registerConfigurationType("cleanupRuleConfig", CleanupRuleConfiguration.class);

            // commit message processors.
            CompositeType commitConfig = registerConfigurationType("commitConfig", CommitMessageConfiguration.class);
            registerConfigurationType("jiraCommitConfig", JiraCommitMessageConfiguration.class);
            registerConfigurationType("customCommitConfig", CustomCommitMessageConfiguration.class);

            commitConfig.addExtension("jiraCommitConfig");
            commitConfig.addExtension("customCommitConfig");

            // change view configuration
            CompositeType changeViewerConfig = registerConfigurationType("changeViewerConfig", ChangeViewerConfiguration.class);
            registerConfigurationType("fisheyeChangeViewerConfig", FisheyeConfiguration.class);
            registerConfigurationType("customChangeViewerConfig", CustomChangeViewerConfiguration.class);

            changeViewerConfig.addExtension("fisheyeChangeViewerConfig");
            changeViewerConfig.addExtension("customChangeViewerConfig");

            CompositeType artifactConfig = registerConfigurationType("artifactConfig", ArtifactConfiguration.class);
            registerConfigurationType("fileArtifactConfig", FileArtifactConfiguration.class);
            registerConfigurationType("directoryArtifactConfig", DirectoryArtifactConfiguration.class);

            artifactConfig.addExtension("fileArtifactConfig");
            artifactConfig.addExtension("directoryArtifactConfig");

            // generated dynamically as new components are registered.
            CompositeType projectConfig = registerConfigurationType("projectConfig", ProjectConfiguration.class);
            projectConfig.addProperty(new TypeProperty("scm", typeRegistry.getType("scmConfig")));
            projectConfig.addProperty(new TypeProperty("type", typeRegistry.getType("typeConfig")));
            projectConfig.addProperty(new TypeProperty("cleanup", typeRegistry.getType("cleanupRuleConfig")));
            projectConfig.addProperty(new TypeProperty("changeViewer", typeRegistry.getType("changeViewerConfig")));

            // scm configuration
            CompositeType scmConfig = typeRegistry.getType("scmConfig");
            registerConfigurationType("svnConfig", SvnConfiguration.class);
            registerConfigurationType("cvsConfig", CvsConfiguration.class);
            registerConfigurationType("perforceConfig", PerforceConfiguration.class);

            // sort out the extensions.
            scmConfig.addExtension("svnConfig");
            scmConfig.addExtension("cvsConfig");
            scmConfig.addExtension("perforceConfig");

            // Triggers
            CompositeType triggerConfig = registerConfigurationType("triggerConfig", TriggerConfiguration.class);
            registerConfigurationType("buildCompletedConfig", BuildCompletedTriggerConfiguration.class);
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

            CompositeType globalConfig = registerConfigurationType("globalConfig", GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public CompositeType registerConfigurationType(Class clazz) throws TypeException
    {
        return registerConfigurationType(null, clazz);
    }

    public CompositeType registerConfigurationType(String name, Class clazz) throws TypeException
    {
        CompositeType type;

        // Type callback that looks for ConfigurationCheck annotations
        TypeHandler handler = new TypeHandler()
        {
            public void handle(CompositeType type) throws TypeException
            {
                ConfigurationCheck annotation = (ConfigurationCheck) type.getAnnotation(ConfigurationCheck.class);
                if (annotation != null)
                {
                    String checkClassName = annotation.value();
                    if (!checkClassName.contains("."))
                    {
                        checkClassName = type.getClazz().getPackage().getName() + "." + checkClassName;
                    }

                    Class checkClass;
                    try
                    {
                        checkClass = type.getClazz().getClassLoader().loadClass(checkClassName);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new TypeException("Registering check type for class '" + type.getClazz().getName() + "': " + e.getMessage(), e);
                    }

                    if(!ConfigurationCheckHandler.class.isAssignableFrom(checkClass))
                    {
                        throw new TypeException("Check type '" + checkClassName + "' does not implement ConfigurationCheckHandler");
                    }

                    CompositeType checkType = typeRegistry.register(checkClass);

                    // FIXME should verify that everything in the check type would land in one form
                    
                    checkTypeMapping.put(type, checkType);
                }
            }
        };

        if (name == null)
        {
            type = typeRegistry.register(clazz, handler);
        }
        else
        {
            type = typeRegistry.register(name, clazz, handler);
        }

        return type;
    }

    public CompositeType getConfigurationCheckType(CompositeType type)
    {
        return checkTypeMapping.get(type);
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
