package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ExtensionTypeProperty;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.ProjectMapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeHandler;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.pulse.prototype.config.CommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.CustomCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.JiraCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.prototype.config.changeviewer.P4WebChangeViewer;
import com.zutubi.pulse.prototype.config.changeviewer.TracChangeViewer;
import com.zutubi.pulse.prototype.config.changeviewer.ViewVCChangeViewer;
import com.zutubi.pulse.prototype.config.misc.LoginConfiguration;
import com.zutubi.pulse.prototype.config.misc.TransientConfiguration;
import com.zutubi.pulse.prototype.config.setup.SetupConfiguration;
import com.zutubi.pulse.prototype.config.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.prototype.config.triggers.CronBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.triggers.TriggerConfiguration;
import com.zutubi.pulse.prototype.config.types.*;
import com.zutubi.pulse.prototype.config.user.AllBuildsConditionConfiguration;
import com.zutubi.pulse.prototype.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.prototype.config.user.SelectedBuildsConditionConfiguration;
import com.zutubi.pulse.prototype.config.user.SubscriptionConditionConfiguration;
import com.zutubi.pulse.prototype.config.user.UnsuccessfulConditionConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.JabberContactConfiguration;
import com.zutubi.pulse.servercore.config.CvsConfiguration;
import com.zutubi.pulse.servercore.config.PerforceConfiguration;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.pulse.servercore.config.SvnConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers the Pulse built-in configuration types.
 */
public class ConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRegistry.class);

    private static final String TRANSIENT_SCOPE = "transient";

    private CompositeType transientConfig;
    private Map<CompositeType, CompositeType> checkTypeMapping = new HashMap<CompositeType, CompositeType>();

    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;

    public void initSetup()
    {
        try
        {
            CompositeType setupConfig = registerConfigurationType(SetupConfiguration.class);
            configurationPersistenceManager.register("setup", setupConfig, false);
        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public void init()
    {
        try
        {
            transientConfig = registerConfigurationType(TransientConfiguration.class);
            configurationPersistenceManager.register(TRANSIENT_SCOPE, transientConfig, false);

            registerTransientConfiguration("login", LoginConfiguration.class);

            CompositeType typeConfig = registerConfigurationType("internal.typeConfig", TypeConfiguration.class);
            registerConfigurationType("internal.antTypeConfig", AntTypeConfiguration.class);
            registerConfigurationType("internal.customTypeConfig", CustomTypeConfiguration.class);
            registerConfigurationType("internal.executableTypeConfig", ExecutableTypeConfiguration.class);
            registerConfigurationType("internal.mavenTypeConfig", MavenTypeConfiguration.class);
            registerConfigurationType("internal.maven2TypeConfig", Maven2TypeConfiguration.class);
            registerConfigurationType("internal.makeTypeConfig", MakeTypeConfiguration.class);
            registerConfigurationType("internal.versionedTypeConfig", VersionedTypeConfiguration.class);
            registerConfigurationType("internal.xcodeTypeConfig", XCodeTypeConfiguration.class);

            typeConfig.addExtension("internal.antTypeConfig");
            typeConfig.addExtension("internal.customTypeConfig");
            typeConfig.addExtension("internal.executableTypeConfig");
            typeConfig.addExtension("internal.mavenTypeConfig");
            typeConfig.addExtension("internal.maven2TypeConfig");
            typeConfig.addExtension("internal.makeTypeConfig");
            typeConfig.addExtension("internal.versionedTypeConfig");
            typeConfig.addExtension("internal.xcodeTypeConfig");

            // change viewer configuration
            CompositeType changeViewerConfig = registerConfigurationType("changeViewerConfig", ChangeViewerConfiguration.class);
            registerConfigurationType("fisheyeChangeViewerConfig", FisheyeConfiguration.class);
            registerConfigurationType("customChangeViewerConfig", CustomChangeViewerConfiguration.class);
            registerConfigurationType("p4WebChangeViewerConfig", P4WebChangeViewer.class);
            registerConfigurationType("tracChangeViewerConfig", TracChangeViewer.class);
            registerConfigurationType("viewVCChangeViewerConfig", ViewVCChangeViewer.class);

            changeViewerConfig.addExtension("fisheyeChangeViewerConfig");
            changeViewerConfig.addExtension("customChangeViewerConfig");
            changeViewerConfig.addExtension("p4WebChangeViewerConfig");
            changeViewerConfig.addExtension("tracChangeViewerConfig");
            changeViewerConfig.addExtension("viewVCChangeViewerConfig");

            // generated dynamically as new components are registered.
            CompositeType projectConfig = registerConfigurationType("projectConfig", ProjectConfiguration.class);

            // scm configuration
            CompositeType scmConfig = typeRegistry.getType(ScmConfiguration.class);
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
            registerConfigurationType("cronTriggerConfig", CronBuildTriggerConfiguration.class);
            registerConfigurationType("scmTriggerConfig", ScmBuildTriggerConfiguration.class);

            triggerConfig.addExtension("buildCompletedConfig");
            triggerConfig.addExtension("cronTriggerConfig");
            triggerConfig.addExtension("scmTriggerConfig");
            
            MapType triggers = new MapType(configurationPersistenceManager);
            triggers.setTypeRegistry(typeRegistry);
            triggers.setCollectionType(typeRegistry.getType("triggerConfig"));
            projectConfig.addProperty(new ExtensionTypeProperty("trigger", triggers));

            // Artifacts.
            CompositeType artifactConfig = registerConfigurationType("artifactConfig", ArtifactConfiguration.class);
            registerConfigurationType("fileArtifactConfig", FileArtifactConfiguration.class);
            registerConfigurationType("directoryArtifactConfig", DirectoryArtifactConfiguration.class);

            artifactConfig.addExtension("fileArtifactConfig");
            artifactConfig.addExtension("directoryArtifactConfig");

//            ListType artifacts = new ListType(configurationPersistenceManager);
//            artifacts.setTypeRegistry(typeRegistry);
//            artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
//            projectConfig.addProperty(new ExtensionTypeProperty("artifact", artifacts));

            // commit message processors.
            CompositeType commitConfig = registerConfigurationType(CommitMessageConfiguration.class);
            registerConfigurationType("jiraCommitConfig", JiraCommitMessageConfiguration.class);
            registerConfigurationType("customCommitConfig", CustomCommitMessageConfiguration.class);

            commitConfig.addExtension("jiraCommitConfig");
            commitConfig.addExtension("customCommitConfig");

            MapType commitTransformers = new MapType(configurationPersistenceManager);
            commitTransformers.setTypeRegistry(typeRegistry);
            commitTransformers.setCollectionType(typeRegistry.getType("commitConfig"));
            projectConfig.addProperty(new ExtensionTypeProperty("commit", commitTransformers));

            // define the root level scope.
            ProjectMapType projectCollection = new ProjectMapType(configurationPersistenceManager);
            projectCollection.setTypeRegistry(typeRegistry);
            projectCollection.setCollectionType(projectConfig);

            configurationPersistenceManager.register("project", projectCollection);

            MapType agentCollection = new MapType(configurationPersistenceManager);
            agentCollection.setTypeRegistry(typeRegistry);
            agentCollection.setCollectionType(registerConfigurationType(AgentConfiguration.class));
            configurationPersistenceManager.register("agent", agentCollection);

            CompositeType globalConfig = registerConfigurationType("globalConfig", GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);

            
            // user configuration.

            MapType userCollection = new MapType(configurationPersistenceManager);
            userCollection.setTypeRegistry(typeRegistry);
            userCollection.setCollectionType(registerConfigurationType(UserConfiguration.class));

            configurationPersistenceManager.register("user", userCollection);

            // contacts configuration
            CompositeType contactConfig = typeRegistry.getType(ContactConfiguration.class);
            registerConfigurationType("emailContactConfig", EmailContactConfiguration.class);
            registerConfigurationType("jabberContactConfig", JabberContactConfiguration.class);

            // sort out the extensions.
            contactConfig.addExtension("emailContactConfig");
            contactConfig.addExtension("jabberContactConfig");


            // user subscription conditions
            CompositeType userSubscriptionConfig = typeRegistry.getType(SubscriptionConditionConfiguration.class);
            registerConfigurationType("internal.allBuildsConditionConfig", AllBuildsConditionConfiguration.class);
            registerConfigurationType("internal.selectedBuildsConditionConfig", SelectedBuildsConditionConfiguration.class);
            registerConfigurationType("internal.customConditionConfig", CustomConditionConfiguration.class);
            registerConfigurationType("internal.unsuccessfulConditionConfig", UnsuccessfulConditionConfiguration.class);

            userSubscriptionConfig.addExtension("internal.allBuildsConditionConfig");
            userSubscriptionConfig.addExtension("internal.selectedBuildsConditionConfig");
            userSubscriptionConfig.addExtension("internal.customConditionConfig");
            userSubscriptionConfig.addExtension("internal.unsuccessfulConditionConfig");

        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public void registerTransientConfiguration(String propertyName, Class clazz) throws TypeException
    {
        CompositeType type = registerConfigurationType(clazz);
        transientConfig.addProperty(new ExtensionTypeProperty(propertyName, type));
    }

    public void registerProjectMapExtension(String name, Class clazz) throws TypeException
    {
        // create the map type.
        MapType mapType = new MapType(configurationPersistenceManager);
        mapType.setTypeRegistry(typeRegistry);

        // register the new type.
        CompositeType type = registerConfigurationType(clazz);
        mapType.setCollectionType(type);

        // register the new type with the project as an extension point.
        CompositeType projectConfig = typeRegistry.getType(ProjectConfiguration.class);
        projectConfig.addProperty(new ExtensionTypeProperty(name, mapType));
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
