package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.bjam.BJamPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.core.*;
import com.zutubi.pulse.core.commands.make.MakePostProcessorConfiguration;
import com.zutubi.pulse.core.commands.maven.MavenPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2CommandConfiguration;
import com.zutubi.pulse.core.commands.maven2.Maven2PostProcessorConfiguration;
import com.zutubi.pulse.core.commands.msbuild.MsBuildPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.nant.NAntPostProcessorConfiguration;
import com.zutubi.pulse.core.commands.xcode.XCodePostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.boostregression.BoostRegressionPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.boosttest.BoostTestReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.cppunit.CppUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.cunit.CUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.gcc.GccPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.nunit.NUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.ocunit.OCUnitReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.qtestlib.QTestLibReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.unittestpp.UnitTestPlusPlusReportPostProcessorConfiguration;
import com.zutubi.pulse.core.postprocessors.visualstudio.VisualStudioPostProcessorConfiguration;
import com.zutubi.pulse.core.scm.git.config.GitConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.user.SetPasswordConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.HandleAllocator;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.ui.actions.ActionManager;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import static com.zutubi.pulse.master.agent.AgentManager.GLOBAL_AGENT_NAME;
import static com.zutubi.pulse.master.agent.AgentManager.MASTER_AGENT_NAME;
import static com.zutubi.pulse.master.model.ProjectManager.GLOBAL_PROJECT_NAME;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.*;
import static org.mockito.Mockito.*;

/**
 * The configuration helper is a support class that bridges the gap between local
 * configuration instances and the remote pulse server.
 * <p/>
 * In particular, it allows you to create local configuration instances and insert
 * them into a remote pulse server by handling the communications overhead.
 */
public class ConfigurationHelper
{
    private boolean initialised = false;
    
    private MasterConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;
    private RemoteApiClient remoteApi;

    private Instantiator instantiator;
    private AtomicLong nextHandle = new AtomicLong(0);

    private String templateOwnerPath = "";

    public void init() throws TypeException
    {
        if (initialised)
        {
            return;
        }
        
        configurationRegistry = new MasterConfigurationRegistry();
        configurationRegistry.setActionManager(new ActionManager());

        // Stub the reference manager to delegate requests to the remote pulse instance.
        ConfigurationReferenceManager referenceManager = mock(ConfigurationReferenceManager.class);
        stub(referenceManager.getReferencedPathForHandle(eq(templateOwnerPath), anyLong())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                long handle = (Long) invocation.getArguments()[1];
                return remoteApi.getConfigPath(String.valueOf(handle));
            }
        });
        stub(referenceManager.getReferenceHandleForPath(anyString(), anyString())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                String path = (String) invocation.getArguments()[1];
                return Long.valueOf(remoteApi.getConfigHandle(path));
            }
        });

        // The local handle allocator is used to generate unique keys for records inserted
        // into lists.  This instance does not need to be in 'sync' with pulse's configuration
        // system handle allocator.
        HandleAllocator handleAllocator = mock(HandleAllocator.class);
        stub(handleAllocator.allocateHandle()).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return nextHandle.incrementAndGet();
            }
        });

        ConfigurationPersistenceManager persistenceManager = mock(ConfigurationPersistenceManager.class);
        ConfigurationSecurityManager securityManager = mock(ConfigurationSecurityManager.class);
        ConfigurationTemplateManager templateManager = mock(ConfigurationTemplateManager.class);

        instantiator = new SimpleInstantiator(templateOwnerPath, referenceManager, templateManager);

        typeRegistry = new TypeRegistry();
        typeRegistry.setConfigurationReferenceManager(referenceManager);
        typeRegistry.setHandleAllocator(handleAllocator);

        configurationRegistry.setConfigurationPersistenceManager(persistenceManager);
        configurationRegistry.setConfigurationSecurityManager(securityManager);
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.init();

        // Configure the plugin configuration types that will be available to the acceptance tests.
        register(AntCommandConfiguration.class);
        register(ExecutableCommandConfiguration.class);
        register(SleepCommandConfiguration.class);
        register(Maven2CommandConfiguration.class);
        register(SubversionConfiguration.class);
        register(GitConfiguration.class);
        register(DependentBuildTriggerConfiguration.class);
        register(AntPostProcessorConfiguration.class);
        register(BoostRegressionPostProcessorConfiguration.class);
        register(BoostTestReportPostProcessorConfiguration.class);
        register(BJamPostProcessorConfiguration.class);
        register(CustomFieldsPostProcessorConfiguration.class);
        register(CppUnitReportPostProcessorConfiguration.class);
        register(CUnitReportPostProcessorConfiguration.class);
        register(GccPostProcessorConfiguration.class);
        register(JUnitEEReportPostProcessorConfiguration.class);
        register(JUnitReportPostProcessorConfiguration.class);
        register(JUnitSummaryPostProcessorConfiguration.class);
        register(MakePostProcessorConfiguration.class);
        register(MavenPostProcessorConfiguration.class);
        register(Maven2PostProcessorConfiguration.class);
        register(MsBuildPostProcessorConfiguration.class);
        register(NAntPostProcessorConfiguration.class);
        register(NUnitReportPostProcessorConfiguration.class);
        register(OCUnitReportPostProcessorConfiguration.class);
        register(QTestLibReportPostProcessorConfiguration.class);
        register(UnitTestPlusPlusReportPostProcessorConfiguration.class);
        register(VisualStudioPostProcessorConfiguration.class);
        register(XCodePostProcessorConfiguration.class);
        
        initialised = true;
    }

    public <T extends Configuration> void register(Class<T> type) throws TypeException
    {
        if (typeRegistry.getType(type) == null)
        {
            configurationRegistry.registerConfigurationType(type);
        }
    }

    /**
     * Get a proxy instance to the pulse master agent.
     *
     * @return a proxy instance to the pulse master agent.
     * @throws Exception thrown on error.
     */
    public AgentConfiguration getMasterAgentReference() throws Exception
    {
        return getAgentReference(MASTER_AGENT_NAME);
    }

    /**
     * Get a proxy instance to the named agent.
     *
     * @param agentName the name of the agent this proxy will reference.
     * @return an agent reference.
     * @throws Exception thrown on error.
     */
    public AgentConfiguration getAgentReference(String agentName) throws Exception
    {
        return getConfigurationReference(AGENTS_SCOPE + "/" + agentName, AgentConfiguration.class);
    }

    /**
     * Get a proxy instance to the named post processor configured in the global template project.
     *
     * @param name  the name of the post processor.
     * @param type  the type of the configuration being referenced.
     * @return  the post processor reference
     * @throws Exception thrown on error.
     */
    public <V extends Configuration> V getPostProcessor(String name, Class<V> type) throws Exception
    {
        return getConfigurationReference(PROJECTS_SCOPE + "/global project template/postProcessors/" + name, type);
    }

    /**
     * Some portions of the configuration system make use of references to existing objects.  In order to
     * configure a reference to a remote configuration instance, you need to 'load' a proxy instance that
     * contains enough details for the configuration system to resolve the reference to the actual instance.
     * This method handles creating such a proxy instance.
     *
     * @param path the path to the configuration being referenced.
     * @param type the type of the configuration being referenced.
     * @param <V>  the class type of the configuration being referenced.
     * @return a proxy instance with enough details configured of Pulse to resolve the reference.
     * @throws Exception thrown on error.
     */
    public <V extends Configuration> V getConfigurationReference(String path, Class<V> type) throws Exception
    {
        V config = type.newInstance();
        config.setConfigurationPath(path);
        config.setHandle(Long.valueOf(remoteApi.getConfigHandle(path)));

        if (config instanceof NamedConfiguration)
        {
            ((NamedConfiguration) config).setName(PathUtils.getBaseName(path));
        }

        return config;
    }

    public <V extends Configuration> V getConfiguration(String path, Class<V> clazz) throws Exception
    {
        CompositeType type = typeRegistry.getType(clazz);
        Hashtable<String, Object> data = remoteApi.getConfig(path);

        MutableRecord record = type.fromXmlRpc(templateOwnerPath, data, true);

        V config = (V) type.instantiate(record, instantiator);
        type.initialise(config, record, instantiator);
        
        config.setConfigurationPath(path);
        config.setHandle(Long.valueOf(remoteApi.getConfigHandle(path)));

        if (config instanceof NamedConfiguration)
        {
            ((NamedConfiguration) config).setName(PathUtils.getBaseName(path));
        }

        return config;
    }

    /**
     * Returns true if a project by the specified name exists.
     *
     * @param projectName   the name of the project we are checking
     * @return true iff the project exists, false otherwise.
     *
     * @throws Exception thrown on error.
     */
    public boolean isProjectExists(String projectName) throws Exception
    {
        return remoteApi.configPathExists(PROJECTS_SCOPE + "/" + projectName);
    }

    /**
     * Convenience method for inserting a new ProjectConfiguration instance.  If the project is not
     * a template, this method will wait for it to initialise before returning.
     *
     * @param project  the project to be inserted.
     * @param template if true, a project template is created, otherwise a
     *                 concrete project is created
     * @return the path at which the project was inserted.
     * @throws Exception thrown on error.
     */
    public String insertProject(ProjectConfiguration project, boolean template) throws Exception
    {
        String globalTemplateProjectPath = PROJECTS_SCOPE + "/" + GLOBAL_PROJECT_NAME;
        String path = insertTemplatedConfig(globalTemplateProjectPath, project, template);
        if (!template)
        {
            remoteApi.waitForProjectToInitialise(project.getName());
        }
        return path;
    }


    public boolean isUserExists(String userName) throws Exception
    {
        return remoteApi.configPathExists(USERS_SCOPE + "/" + userName);
    }

    public void insertUser(UserConfiguration user) throws Exception
    {
        String path = USERS_SCOPE + "/" + user.getName();
        insertConfig(USERS_SCOPE, user);

        // set the password.
        Hashtable <String, Object> password = remoteApi.createEmptyConfig(SetPasswordConfiguration.class);
        password.put("password", user.getPassword());
        password.put("confirmPassword", user.getPassword());
        remoteApi.doConfigActionWithArgument(path, "setPassword", password);
    }

    public boolean isAgentExists(String agentName) throws Exception
    {
        return remoteApi.configPathExists(AGENTS_SCOPE + "/" + agentName);
    }

    /**
     * Convenience method for inserting a new AgentConfiguration instance.
     *
     * @param agent the agent to be inserted.
     * @return  the path at which the agent was inserted.
     * @throws Exception thrown on error
     */
    public String insertAgent(AgentConfiguration agent) throws Exception
    {
        String globalTemplateAgentPath = AGENTS_SCOPE + "/" + GLOBAL_AGENT_NAME;
        return insertTemplatedConfig(globalTemplateAgentPath, agent, false);
    }

    /**
     * Insert a configuration instance into a templated scope.
     *
     * @param parentTemplatePath the path of the templated parent for the configuration instance
     * @param config             the configuration instance.
     * @param template           if true, the configuration should be marked as
     *                           a template, if false it should be concrete
     * @return the path at which the configuration instance was inserted.
     * @throws Exception thrown on error.
     */
    public String insertTemplatedConfig(String parentTemplatePath, Configuration config, boolean template) throws Exception
    {
        Hashtable<String, Object> data = toXmlRpc(config);
        String insertedPath = remoteApi.insertTemplatedConfig(parentTemplatePath, data, template);

        // This step updates the configuration with configuration path and handle details.
        updatePathsAndHandles(config, insertedPath, data);

        return insertedPath;
    }

    /**
     * Insert a concrete configuration instance into a particular path.
     *
     * @param path      the path at which the configuration will be inserted.
     * @param config    the configuration to be inserted.
     * @return  the path at which teh configuration instance was inserted.
     * @throws Exception throws on error.
     */
    public String insertConfig(String path, Configuration config) throws Exception
    {
        Hashtable<String, Object> data = toXmlRpc(config);
        String insertedPath = remoteApi.insertConfig(path, data);
        updatePathsAndHandles(config, insertedPath, data);
        return insertedPath;
    }

    /**
     * Convenience method for updating an existing configuration, including
     * nested configurations.
     *
     * @param configuration the configuration to be updated.
     * @return the path at which the configuration was updated.
     * @throws Exception thrown on error
     *
     * @see #update(Configuration, boolean) 
     */
    public String update(Configuration configuration) throws Exception
    {
        return update(configuration, true);
    }

    /**
     * Convenience method for updating an existing configuration.
     *
     * @param configuration the configuration to be updated.
     * @param deep          indicates whether or not the configurations nested within
     *                      specified configuration should also be updated.
     * @return the path at which the configuration was updated.
     * @throws Exception thrown on error
     */
    public String update(Configuration configuration, boolean deep) throws Exception
    {
        Hashtable<String, Object> data = toXmlRpc(configuration);
        return remoteApi.saveConfig(configuration.getConfigurationPath(), data, deep);
    }

    /**
     * Convert the configuration instance into a form that is compatible with xml rpc.
     *
     * @param config the configuration instance being converted.
     * @return a hashtable representation of the configuration instance
     * @throws Exception on error
     */
    private Hashtable<String, Object> toXmlRpc(Configuration config) throws Exception
    {
        CompositeType type = typeRegistry.getType(config.getClass());
        return type.toXmlRpc(templateOwnerPath, type.unstantiate(config, templateOwnerPath));
    }

    private void updatePathsAndHandles(Configuration config, String path, Hashtable<String, Object> data) throws Exception
    {
        config.setConfigurationPath(path);
        config.setHandle(Long.valueOf(remoteApi.getConfigHandle(path)));

        // time to fill in the configuration paths and handles.  Remember to not traverse across references
        CompositeType type = typeRegistry.getType(config.getClass());
        String basePath = config.getConfigurationPath();

        for (String propertyName : type.getNestedPropertyNames())
        {
            TypeProperty property = type.getProperty(propertyName);
            if (property.getAnnotation(Reference.class) != null)
            {
                // skip references.
                continue;
            }

            if (property.getType() instanceof ListType)
            {
                List collection = (List) property.getValue(config);
                if (collection != null)
                {
                    Vector collectionData = (Vector) data.get(propertyName);
                    for (Object o : collection)
                    {
                        // how to map the collection data to the configuration instance so that we
                        // can extract the path...  for now, don't update lists.
                        // We don't have enough data locally, so somehow need to load the data from pulse.
                        // updatePathsAndHandles((Configuration) o, null);
                    }
                }
            }
            else if (property.getType() instanceof MapType)
            {
                Map collection = (Map) property.getValue(config);
                if (collection != null)
                {
                    Hashtable<String, Object> collectionData = (Hashtable<String, Object>) data.get(propertyName);
                    for (Object o : collection.keySet())
                    {
                        String key = (String) o;
                        Configuration value = (Configuration) collection.get(key);
                        String configPath = basePath + "/" + propertyName + "/" + key;
                        updatePathsAndHandles(value, configPath, (Hashtable<String, Object>) collectionData.get(key));
                    }
                }
            }
            else
            {
                Configuration nestedConfig = (Configuration) property.getValue(config);
                if (nestedConfig != null)
                {
                    String configPath = basePath + "/" + propertyName;
                    updatePathsAndHandles(nestedConfig, configPath, (Hashtable<String, Object>) data.get(propertyName));
                }
            }
        }
    }
    
    public void setRemoteApi(RemoteApiClient remoteApi)
    {
        this.remoteApi = remoteApi;
    }
}
