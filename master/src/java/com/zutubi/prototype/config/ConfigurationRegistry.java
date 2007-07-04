package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.type.CompositeType;
import com.zutubi.prototype.type.ExtensionTypeProperty;
import com.zutubi.prototype.type.MapType;
import com.zutubi.prototype.type.TemplatedMapType;
import com.zutubi.prototype.type.TypeException;
import com.zutubi.prototype.type.TypeHandler;
import com.zutubi.prototype.type.TypeRegistry;
import com.zutubi.prototype.type.record.HandleAllocator;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.misc.LoginConfiguration;
import com.zutubi.pulse.prototype.config.misc.TransientConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.CustomChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.FisheyeConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.P4WebChangeViewer;
import com.zutubi.pulse.prototype.config.project.changeviewer.TracChangeViewer;
import com.zutubi.pulse.prototype.config.project.changeviewer.ViewVCChangeViewer;
import com.zutubi.pulse.prototype.config.project.commit.CommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.commit.CustomCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.commit.JiraCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.CronBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.types.*;
import com.zutubi.pulse.prototype.config.setup.SetupConfiguration;
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
import com.zutubi.pulse.cleanup.config.CleanupConfiguration;
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
    private HandleAllocator handleAllocator;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public void initSetup()
    {
        try
        {
            CompositeType setupConfig = registerConfigurationType(SetupConfiguration.class);
            configurationPersistenceManager.register("init", setupConfig, false);
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

            CompositeType typeConfig = registerConfigurationType(TypeConfiguration.class);
            registerConfigurationType(AntTypeConfiguration.class);
            registerConfigurationType(CustomTypeConfiguration.class);
            registerConfigurationType(ExecutableTypeConfiguration.class);
            registerConfigurationType(MavenTypeConfiguration.class);
            registerConfigurationType(Maven2TypeConfiguration.class);
            registerConfigurationType(MakeTypeConfiguration.class);
            registerConfigurationType(VersionedTypeConfiguration.class);
            registerConfigurationType(XCodeTypeConfiguration.class);

            typeConfig.addExtension("zutubi.antTypeConfig");
            typeConfig.addExtension("zutubi.customTypeConfig");
            typeConfig.addExtension("zutubi.executableTypeConfig");
            typeConfig.addExtension("zutubi.mavenTypeConfig");
            typeConfig.addExtension("zutubi.maven2TypeConfig");
            typeConfig.addExtension("zutubi.makeTypeConfig");
            typeConfig.addExtension("zutubi.versionedTypeConfig");
            typeConfig.addExtension("zutubi.xcodeTypeConfig");

            // change viewer configuration
            CompositeType changeViewerConfig = registerConfigurationType(ChangeViewerConfiguration.class);
            registerConfigurationType(FisheyeConfiguration.class);
            registerConfigurationType(CustomChangeViewerConfiguration.class);
            registerConfigurationType(P4WebChangeViewer.class);
            registerConfigurationType(TracChangeViewer.class);
            registerConfigurationType(ViewVCChangeViewer.class);

            changeViewerConfig.addExtension("zutubi.fisheyeChangeViewerConfig");
            changeViewerConfig.addExtension("zutubi.customChangeViewerConfig");
            changeViewerConfig.addExtension("zutubi.p4WebChangeViewerConfig");
            changeViewerConfig.addExtension("zutubi.tracChangeViewerConfig");
            changeViewerConfig.addExtension("zutubi.viewVCChangeViewerConfig");

            // generated dynamically as new components are registered.
            CompositeType projectConfig = registerConfigurationType(ProjectConfiguration.class);

            // scm configuration
            CompositeType scmConfig = typeRegistry.getType(ScmConfiguration.class);
            registerConfigurationType(SvnConfiguration.class);
            registerConfigurationType(CvsConfiguration.class);
            registerConfigurationType(PerforceConfiguration.class);

            // sort out the extensions.
            scmConfig.addExtension("zutubi.svnConfig");
            scmConfig.addExtension("zutubi.cvsConfig");
            scmConfig.addExtension("zutubi.perforceConfig");

            // Triggers
            CompositeType triggerConfig = registerConfigurationType(TriggerConfiguration.class);
            registerConfigurationType(BuildCompletedTriggerConfiguration.class);
            registerConfigurationType(CronBuildTriggerConfiguration.class);
            registerConfigurationType(ScmBuildTriggerConfiguration.class);

            triggerConfig.addExtension("zutubi.buildCompletedConfig");
            triggerConfig.addExtension("zutubi.cronTriggerConfig");
            triggerConfig.addExtension("zutubi.scmTriggerConfig");
            
            MapType triggers = new MapType();
            triggers.setTypeRegistry(typeRegistry);
            triggers.setCollectionType(triggerConfig);
            projectConfig.addProperty(new ExtensionTypeProperty("trigger", triggers));

            // Artifacts.
            CompositeType artifactConfig = registerConfigurationType(ArtifactConfiguration.class);
            registerConfigurationType(FileArtifactConfiguration.class);
            registerConfigurationType(DirectoryArtifactConfiguration.class);

            artifactConfig.addExtension("zutubi.fileArtifactConfig");
            artifactConfig.addExtension("zutubi.directoryArtifactConfig");

//            ListType artifacts = new ListType(configurationPersistenceManager);
//            artifacts.setTypeRegistry(typeRegistry);
//            artifacts.setCollectionType(typeRegistry.getType("artifactConfig"));
//            projectConfig.addProperty(new ExtensionTypeProperty("artifact", artifacts));

            // commit message processors.
            CompositeType commitConfig = registerConfigurationType(CommitMessageConfiguration.class);
            registerConfigurationType(JiraCommitMessageConfiguration.class);
            registerConfigurationType(CustomCommitMessageConfiguration.class);

            commitConfig.addExtension("zutubi.jiraCommitMessageConfig");
            commitConfig.addExtension("zutubi.customCommitMessageConfig");

            MapType commitTransformers = new MapType();
            commitTransformers.setTypeRegistry(typeRegistry);
            commitTransformers.setCollectionType(commitConfig);
            projectConfig.addProperty(new ExtensionTypeProperty("commit", commitTransformers));

            // define the root level scope.
            TemplatedMapType projectCollection = new TemplatedMapType();
            projectCollection.setTypeRegistry(typeRegistry);
            projectCollection.setCollectionType(projectConfig);

            configurationPersistenceManager.register("project", projectCollection);

            // register project configuration.  This will eventually be handled as an extension point
            registerProjectMapExtension("cleanup", CleanupConfiguration.class);
            
            TemplatedMapType agentCollection = new TemplatedMapType();
            agentCollection.setTypeRegistry(typeRegistry);
            agentCollection.setCollectionType(registerConfigurationType(AgentConfiguration.class));
            configurationPersistenceManager.register("agent", agentCollection);

            CompositeType globalConfig = registerConfigurationType(GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);

            
            // user configuration.

            MapType userCollection = new MapType();
            userCollection.setTypeRegistry(typeRegistry);
            userCollection.setCollectionType(registerConfigurationType(UserConfiguration.class));

            configurationPersistenceManager.register("user", userCollection);

            // contacts configuration
            CompositeType contactConfig = typeRegistry.getType(ContactConfiguration.class);
            registerConfigurationType(EmailContactConfiguration.class);
            registerConfigurationType(JabberContactConfiguration.class);

            // sort out the extensions.
            contactConfig.addExtension("zutubi.emailContactConfig");
            contactConfig.addExtension("zutubi.jabberContactConfig");

            // user subscription conditions
            CompositeType userSubscriptionConfig = typeRegistry.getType(SubscriptionConditionConfiguration.class);
            registerConfigurationType(AllBuildsConditionConfiguration.class);
            registerConfigurationType(SelectedBuildsConditionConfiguration.class);
            registerConfigurationType(CustomConditionConfiguration.class);
            registerConfigurationType(UnsuccessfulConditionConfiguration.class);

            userSubscriptionConfig.addExtension("zutubi.allBuildsConditionConfig");
            userSubscriptionConfig.addExtension("zutubi.selectedBuildsConditionConfig");
            userSubscriptionConfig.addExtension("zutubi.customConditionConfig");
            userSubscriptionConfig.addExtension("zutubi.unsuccessfulConditionConfig");

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

    private void registerProjectMapExtension(String name, Class clazz) throws TypeException
    {
        // create the map type.
        MapType mapType = new MapType();
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
        return configurationTemplateManager.getInstance(GlobalConfiguration.SCOPE_NAME, GlobalConfiguration.class);
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setHandleAllocator(HandleAllocator handleAllocator)
    {
        this.handleAllocator = handleAllocator;
    }
}
