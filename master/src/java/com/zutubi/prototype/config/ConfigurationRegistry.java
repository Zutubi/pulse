package com.zutubi.prototype.config;

import com.zutubi.config.annotations.ConfigurationCheck;
import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.prototype.type.*;
import com.zutubi.pulse.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.prototype.config.misc.LoginConfiguration;
import com.zutubi.pulse.prototype.config.misc.TransientConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.*;
import com.zutubi.pulse.prototype.config.project.commit.CommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.commit.CustomCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.commit.JiraCommitMessageConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.BuildCompletedTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.CronBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.ScmBuildTriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.triggers.TriggerConfiguration;
import com.zutubi.pulse.prototype.config.project.types.*;
import com.zutubi.pulse.prototype.config.setup.SetupConfiguration;
import com.zutubi.pulse.prototype.config.user.*;
import com.zutubi.pulse.prototype.config.user.contacts.ContactConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.prototype.config.user.contacts.JabberContactConfiguration;
import com.zutubi.pulse.servercore.scm.cvs.config.CvsConfiguration;
import com.zutubi.pulse.servercore.scm.svn.config.SvnConfiguration;
import com.zutubi.pulse.servercore.scm.p4.config.PerforceConfiguration;
import com.zutubi.pulse.servercore.scm.config.ScmConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers the Pulse built-in configuration types.
 */
@SuppressWarnings({ "unchecked" })
public class ConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(ConfigurationRegistry.class);

    private static final String TRANSIENT_SCOPE = "transient";

    public static final String AGENTS_SCOPE = "agents";
    public static final String PROJECTS_SCOPE = "projects";
    public static final String SETUP_SCOPE = "init";
    public static final String USERS_SCOPE = "users";

    private CompositeType transientConfig;
    private Map<CompositeType, CompositeType> checkTypeMapping = new HashMap<CompositeType, CompositeType>();

    private TypeRegistry typeRegistry;
    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ConfigurationTemplateManager configurationTemplateManager;

    public void initSetup()
    {
        try
        {
            CompositeType setupConfig = registerConfigurationType(SetupConfiguration.class);
            configurationPersistenceManager.register(SETUP_SCOPE, setupConfig, false);
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

            configurationPersistenceManager.register(PROJECTS_SCOPE, projectCollection);

            // register project configuration.  This will eventually be handled as an extension point
            registerProjectMapExtension("cleanup", CleanupConfiguration.class);
            
            TemplatedMapType agentCollection = new TemplatedMapType();
            agentCollection.setTypeRegistry(typeRegistry);
            agentCollection.setCollectionType(registerConfigurationType(AgentConfiguration.class));
            configurationPersistenceManager.register(AGENTS_SCOPE, agentCollection);

            CompositeType globalConfig = registerConfigurationType(GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);

            
            // user configuration.

            MapType userCollection = new MapType();
            userCollection.setTypeRegistry(typeRegistry);
            userCollection.setCollectionType(registerConfigurationType(UserConfiguration.class));

            configurationPersistenceManager.register(USERS_SCOPE, userCollection);

            // contacts configuration
            CompositeType contactConfig = typeRegistry.getType(ContactConfiguration.class);
            registerConfigurationType(EmailContactConfiguration.class);
            registerConfigurationType(JabberContactConfiguration.class);

            // sort out the extensions.
            contactConfig.addExtension("zutubi.emailContactConfig");
            contactConfig.addExtension("zutubi.jabberContactConfig");

            // user subscriptions
            CompositeType userSubscriptionConfig = typeRegistry.getType(SubscriptionConfiguration.class);
            registerConfigurationType(ProjectSubscriptionConfiguration.class);
            registerConfigurationType(PersonalSubscriptionConfiguration.class);
            userSubscriptionConfig.addExtension("zutubi.projectSubscriptionConfig");
            userSubscriptionConfig.addExtension("zutubi.personalSubscriptionConfig");
            
            // user subscription conditions
            CompositeType userSubscriptionConditionConfig = typeRegistry.getType(SubscriptionConditionConfiguration.class);
            registerConfigurationType(AllBuildsConditionConfiguration.class);
            registerConfigurationType(SelectedBuildsConditionConfiguration.class);
            registerConfigurationType(CustomConditionConfiguration.class);
            registerConfigurationType(RepeatedUnsuccessfulConditionConfiguration.class);

            userSubscriptionConditionConfig.addExtension("zutubi.allBuildsConditionConfig");
            userSubscriptionConditionConfig.addExtension("zutubi.selectedBuildsConditionConfig");
            userSubscriptionConditionConfig.addExtension("zutubi.customConditionConfig");
            userSubscriptionConditionConfig.addExtension("zutubi.repeatedUnsuccessfulConditionConfig");

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

    public <T extends Configuration> CompositeType registerConfigurationType(Class<T> clazz) throws TypeException
    {
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

        return typeRegistry.register(clazz, handler);
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
}
