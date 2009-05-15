package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.core.scm.config.api.PollableScmConfiguration;
import com.zutubi.pulse.core.tove.config.CoreConfigurationRegistry;
import com.zutubi.pulse.master.cleanup.config.CleanupConfiguration;
import com.zutubi.pulse.master.security.GlobalAuthorityProvider;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.group.AbstractGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.misc.LoginConfiguration;
import com.zutubi.pulse.master.tove.config.misc.TransientConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependenciesConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectTypeSelectionConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.*;
import com.zutubi.pulse.master.tove.config.project.commit.CustomTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.LinkTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.*;
import com.zutubi.pulse.master.tove.config.project.triggers.*;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.MultiRecipeTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.VersionedTypeConfiguration;
import com.zutubi.pulse.master.tove.config.setup.SetupConfiguration;
import com.zutubi.pulse.master.tove.config.user.*;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.master.tove.config.user.contacts.JabberContactConfiguration;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.actions.ConfigurationAction;
import com.zutubi.tove.actions.ConfigurationActions;
import com.zutubi.tove.annotations.ConfigurationCheck;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.ConfigurationCheckHandler;
import com.zutubi.tove.config.api.ConfigurationCreator;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers the Pulse built-in configuration types.
 */
public class MasterConfigurationRegistry extends CoreConfigurationRegistry
{
    private static final Logger LOG = Logger.getLogger(MasterConfigurationRegistry.class);

    public static final String TRANSIENT_SCOPE = "transient";

    public static final String AGENTS_SCOPE = "agents";
    public static final String PROJECTS_SCOPE = "projects";
    public static final String SETUP_SCOPE = "init";
    public static final String USERS_SCOPE = "users";
    public static final String GROUPS_SCOPE = "groups";

    public static final String EXTENSION_PROJECT_TRIGGERS = "triggers";
    public static final String EXTENSION_PROJECT_CLEANUP = "cleanup";

    private CompositeType transientConfig;
    private Map<CompositeType, CompositeType> checkTypeMapping = new HashMap<CompositeType, CompositeType>();

    private ConfigurationPersistenceManager configurationPersistenceManager;
    private ActionManager actionManager;
    private ConfigurationSecurityManager configurationSecurityManager;

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
        super.init();

        try
        {
            // Security
            configurationSecurityManager.registerGlobalPermission(USERS_SCOPE, AccessManager.ACTION_CREATE, GlobalAuthorityProvider.CREATE_USER);
            configurationSecurityManager.registerGlobalPermission(PROJECTS_SCOPE, AccessManager.ACTION_CREATE, ServerPermission.CREATE_PROJECT.toString());
            configurationSecurityManager.registerGlobalPermission(PathUtils.getPath(PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), AccessManager.ACTION_CREATE, ServerPermission.CREATE_PROJECT.toString());
            configurationSecurityManager.registerGlobalPermission(PathUtils.getPath(PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT), AccessManager.ACTION_DELETE, ServerPermission.DELETE_PROJECT.toString());

            configurationSecurityManager.registerOwnedScope(AGENTS_SCOPE);
            configurationSecurityManager.registerOwnedScope(GROUPS_SCOPE);
            configurationSecurityManager.registerOwnedScope(PROJECTS_SCOPE);
            configurationSecurityManager.registerOwnedScope(USERS_SCOPE);
            
            // Types
            transientConfig = registerConfigurationType(TransientConfiguration.class);
            configurationPersistenceManager.register(TRANSIENT_SCOPE, transientConfig, false);

            registerTransientConfiguration("login", LoginConfiguration.class);
            registerTransientConfiguration("signup", SignupUserConfiguration.class);
            registerTransientConfiguration("projectTypeSelection", ProjectTypeSelectionConfiguration.class);

            registerConfigurationType(TypeConfiguration.class);
            registerConfigurationType(CustomTypeConfiguration.class);
            registerConfigurationType(MultiRecipeTypeConfiguration.class);
            registerConfigurationType(VersionedTypeConfiguration.class);

            // change viewer configuration
            registerConfigurationType(ChangeViewerConfiguration.class);
            registerConfigurationType(BasePathChangeViewer.class);
            registerConfigurationType(FisheyeConfiguration.class);
            registerConfigurationType(CustomChangeViewerConfiguration.class);
            registerConfigurationType(P4WebChangeViewer.class);
            registerConfigurationType(AbstractTracChangeViewer.class);
            registerConfigurationType(Trac10ChangeViewer.class);
            registerConfigurationType(Trac11ChangeViewer.class);
            registerConfigurationType(ViewVCChangeViewer.class);

            // generated dynamically as new components are registered.
            CompositeType projectConfig = registerConfigurationType(ProjectConfiguration.class);

            registerConfigurationType(DependenciesConfiguration.class);

            // scm configuration - registration of extensions occurs via plugins.
            registerConfigurationType(PollableScmConfiguration.class);

            // Triggers
            CompositeType triggerConfig = registerConfigurationType(TriggerConfiguration.class);
            registerConfigurationType(BuildCompletedTriggerConfiguration.class);
            registerConfigurationType(CronBuildTriggerConfiguration.class);
            registerConfigurationType(ScmBuildTriggerConfiguration.class);
            registerConfigurationType(DependentBuildTriggerConfiguration.class);

            registerProjectMapExtension(EXTENSION_PROJECT_TRIGGERS, TriggerConfiguration.class);

            // commit message processors.
            registerConfigurationType(CustomTransformerConfiguration.class);
            registerConfigurationType(LinkTransformerConfiguration.class);

            // hooks
            registerConfigurationType(ManualBuildHookConfiguration.class);
            registerConfigurationType(AutoBuildHookConfiguration.class);
            registerConfigurationType(PreBuildHookConfiguration.class);
            registerConfigurationType(PostBuildHookConfiguration.class);
            registerConfigurationType(PostStageHookConfiguration.class);
            registerConfigurationType(RunExecutableTaskConfiguration.class);
            
            // define the root level scope.
            TemplatedMapType projectCollection = new TemplatedMapType(projectConfig, typeRegistry);
            configurationPersistenceManager.register(PROJECTS_SCOPE, projectCollection);

            // register cleanup configuration.  This will eventually be handled as an extension point
            registerProjectMapExtension(EXTENSION_PROJECT_CLEANUP, CleanupConfiguration.class);

            TemplatedMapType agentCollection = new TemplatedMapType(registerConfigurationType(AgentConfiguration.class), typeRegistry);
            configurationPersistenceManager.register(AGENTS_SCOPE, agentCollection);

            CompositeType globalConfig = registerConfigurationType(GlobalConfiguration.class);
            configurationPersistenceManager.register(GlobalConfiguration.SCOPE_NAME, globalConfig);

            // user configuration.

            MapType userCollection = new MapType(registerConfigurationType(UserConfiguration.class), typeRegistry);
            configurationPersistenceManager.register(USERS_SCOPE, userCollection);

            // contacts configuration
            registerConfigurationType(EmailContactConfiguration.class);
            registerConfigurationType(JabberContactConfiguration.class);

            // user subscriptions
            registerConfigurationType(ProjectSubscriptionConfiguration.class);
            registerConfigurationType(PersonalSubscriptionConfiguration.class);

            // user subscription conditions
            registerConfigurationType(AllBuildsConditionConfiguration.class);
            registerConfigurationType(SelectedBuildsConditionConfiguration.class);
            registerConfigurationType(CustomConditionConfiguration.class);
            registerConfigurationType(RepeatedUnsuccessfulConditionConfiguration.class);

            // group configuration .

            CompositeType groupConfig = registerConfigurationType(AbstractGroupConfiguration.class);
            registerConfigurationType(GroupConfiguration.class);
            registerConfigurationType(BuiltinGroupConfiguration.class);

            MapType groupCollection = new MapType(groupConfig, typeRegistry);
            configurationPersistenceManager.register(GROUPS_SCOPE, groupCollection);

        }
        catch (TypeException e)
        {
            LOG.severe(e);
        }
    }

    public void registerTransientConfiguration(String propertyName, Class clazz) throws TypeException
    {
        @SuppressWarnings({"unchecked"})
        CompositeType type = registerConfigurationType(clazz);
        transientConfig.addProperty(new ExtensionTypeProperty(propertyName, type));
    }

    private void registerProjectMapExtension(String name, Class clazz) throws TypeException
    {
        // register the new type.
        @SuppressWarnings({"unchecked"})
        CompositeType type = registerConfigurationType(clazz);

        // create the map type.
        MapType mapType = new MapType(type, typeRegistry);

        // register the new type with the project as an extension point.
        CompositeType projectConfig = typeRegistry.getType(ProjectConfiguration.class);
        projectConfig.addProperty(new ExtensionTypeProperty(name, mapType));
    }

    public <T extends Configuration> CompositeType registerConfigurationType(final Class<T> clazz) throws TypeException
    {
        // Type callback that looks for associated types (check annotations,
        // creators, actions etc).
        TypeHandler handler = new TypeHandler()
        {
            public void handle(CompositeType type) throws TypeException
            {
                ConfigurationCheck annotation = type.getAnnotation(ConfigurationCheck.class, false);
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

                    if (!ConfigurationCheckHandler.class.isAssignableFrom(checkClass))
                    {
                        throw new TypeException("Check type '" + checkClassName + "' does not implement ConfigurationCheckHandler");
                    }

                    @SuppressWarnings({"unchecked"})
                    CompositeType checkType = typeRegistry.register(checkClass);

                    // TODO should verify that everything in the check type would land in one form
                    checkTypeMapping.put(type, checkType);
                }

                Class<? extends Configuration> creatorClass = ConventionSupport.getCreator(type);
                if(creatorClass != null)
                {
                    if(!ConfigurationCreator.class.isAssignableFrom(creatorClass))
                    {
                        throw new TypeException("Creator type '" + creatorClass.getName() + "' does not implement ConfigurationCreator");
                    }

                    typeRegistry.register(creatorClass);
                }

                Class actionsClass = ConventionSupport.getActions(clazz);
                if(actionsClass != null)
                {
                    ConfigurationActions configurationActions = actionManager.getConfigurationActions(type);
                    for(ConfigurationAction action: configurationActions.getAvailableActions())
                    {
                        if(action.hasArgument())
                        {
                            registerTransientConfiguration(getActionProperty(type, action.getName()), action.getArgumentClass());
                        }
                    }
                }
            }
        };

        return typeRegistry.register(clazz, handler);
    }

    public static String getActionPath(CompositeType configurationType, String actionName)
    {
        return PathUtils.getPath(TRANSIENT_SCOPE,  configurationType.getSymbolicName() + ".actions." + actionName);
    }

    public static String getActionProperty(CompositeType configurationType, String actionName)
    {
        return configurationType.getSymbolicName() + ".actions." + actionName;
    }

    public CompositeType getConfigurationCheckType(CompositeType type)
    {
        return checkTypeMapping.get(type);
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}
