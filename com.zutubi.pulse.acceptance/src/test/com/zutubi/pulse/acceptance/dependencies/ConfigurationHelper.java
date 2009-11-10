package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.XmlRpcHelper;
import com.zutubi.pulse.core.commands.ant.AntCommandConfiguration;
import com.zutubi.pulse.core.commands.ant.AntPostProcessorConfiguration;
import com.zutubi.pulse.core.scm.svn.config.SubversionConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.DependentBuildTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.config.ConfigurationPersistenceManager;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.type.*;
import com.zutubi.tove.type.record.HandleAllocator;
import com.zutubi.tove.type.record.PathUtils;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The configuration helper is a support class that bridges the gap between local
 * configuration instances and the remote pulse server.
 *
 * In particular, it allows you to create local configuration instances and insert
 * them into a remote pulse server by handling the communications overhead.
 */
public class ConfigurationHelper
{
    private MasterConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;
    private XmlRpcHelper xmlRpcHelper;
    private ConfigurationReferenceManager referenceManager;

    private HandleAllocator handleAllocator;
    private AtomicLong nextHandle = new AtomicLong(0);

    private String templateOwnerPath = "";

    public void init() throws TypeException
    {
        configurationRegistry = new MasterConfigurationRegistry();
        configurationRegistry.setActionManager(new ActionManager());

        // Stub the reference manager to delegate requests to the remote pulse instance.
        referenceManager = mock(ConfigurationReferenceManager.class);
        stub(referenceManager.getReferencedPathForHandle(eq(templateOwnerPath), anyLong())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                long handle = (Long)invocation.getArguments()[1];
                return xmlRpcHelper.getConfigPath(String.valueOf(handle));
            }
        });
        stub(referenceManager.getReferenceHandleForPath(anyString())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                String path= (String) invocation.getArguments()[0];
                return xmlRpcHelper.getConfigHandle(path);
            }
        });

        // The local handle allocator is used to generate unique keys for records inserted
        // into lists.  This instance does not need to be in 'sync' with pulse's configuration
        // system handle allocator.
        handleAllocator = mock(HandleAllocator.class);
        stub(handleAllocator.allocateHandle()).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return nextHandle.incrementAndGet();
            }
        });

        ConfigurationPersistenceManager persistenceManager = mock(ConfigurationPersistenceManager.class);
        ConfigurationSecurityManager securityManager = mock(ConfigurationSecurityManager.class);

        typeRegistry = new TypeRegistry();
        typeRegistry.setConfigurationReferenceManager(referenceManager);
        typeRegistry.setHandleAllocator(handleAllocator);

        configurationRegistry.setConfigurationPersistenceManager(persistenceManager);
        configurationRegistry.setConfigurationSecurityManager(securityManager);
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.init();

        // Configure the plugin configuration types that will be available to the acceptance
        // tests.
        configurationRegistry.registerConfigurationType(AntCommandConfiguration.class);
        configurationRegistry.registerConfigurationType(AntPostProcessorConfiguration.class);
        configurationRegistry.registerConfigurationType(SubversionConfiguration.class);
        configurationRegistry.registerConfigurationType(DependentBuildTriggerConfiguration.class);
    }

    /**
     * Get a proxy instance to the pulse master agent.
     * @return  a proxy instance to the pulse master agent.
     * 
     * @throws Exception thrown on error.
     */
    public AgentConfiguration getMasterAgentReference() throws Exception
    {
        return getAgentReference("master");
    }

    /**
     * Get a prxy instance to the named agent.
     * @param agentName     the name of the agent this proxy will reference.
     * @return an agent reference.
     *
     * @throws Exception thrown on error.
     */
    public AgentConfiguration getAgentReference(String agentName) throws Exception
    {
        return getConfigurationReference("agents/" + agentName, AgentConfiguration.class);
    }

    /**
     * Some portions of the configuration system make use of references to existing objects.  In order to
     * configure a reference to a remote configuration instance, you need to 'load' a proxy instance that
     * contains enough details for the configuration system to resolve the reference to the actual instance.
     * This method handles creating such a proxy instance.
     *
     * @param path  the path to the configuration being referenced.
     * @param type  the type of the configuration being referenced.
     * @param <V>   the class type of the configuration being referenced.
     * @return  a proxy instance with enough details configured of Pulse to resolve the reference.
     *
     * @throws Exception thrown on error.
     */
    public <V extends Configuration> V getConfigurationReference(String path, Class<V> type) throws Exception
    {
        V config = type.newInstance();
        config.setConfigurationPath(path);
        config.setHandle(Long.valueOf(xmlRpcHelper.getConfigHandle(path)));

        if (config instanceof NamedConfiguration)
        {
            ((NamedConfiguration)config).setName(PathUtils.getBaseName(path));
        }

        return config;
    }

    /**
     * Convenience method for inserting a new ProjectConfiguration instance.
     *
     * @param project   the project to be inserted.
     * @return the path at which the project was inserted.
     *
     * @throws Exception thrown on error.
     */
    public String insertProject(ProjectConfiguration project) throws Exception
    {
        String globalTemplateProjectPath = MasterConfigurationRegistry.PROJECTS_SCOPE + "/" + ProjectManager.GLOBAL_PROJECT_NAME;
        return insertTemplatedConfig(globalTemplateProjectPath, project);
    }

    /**
     * Insert a concreate configuration instance into a templated scope.
     *
     * @param parentTemplatePath    the path of the templated parent for the configuration instance
     * @param config    the configuration instance.
     * @return the path at which the configuration instance was inserted.
     * 
     * @throws Exception thrown on error.
     */
    public String insertTemplatedConfig(String parentTemplatePath, Configuration config) throws Exception
    {
        Hashtable<String, Object> data = toXmlRpc(config);
        String insertedPath = xmlRpcHelper.insertTemplatedConfig(parentTemplatePath, data, false);

        // This step updates the configuration with configuration path and handle details.
        updatePathsAndHandles(config, insertedPath, data);

        return insertedPath;
    }

    /**
     * Convenience method for updating an existing project configuration.
     *
     * @param project   the updated project configuration
     * @return the path at which the configuration was updated.
     *
     * @throws Exception thrown on error
     */
    public String updateProject(ProjectConfiguration project) throws Exception
    {
        Hashtable<String, Object> data = toXmlRpc(project);
        return xmlRpcHelper.saveConfig(project.getConfigurationPath(), data, true);
    }

    /**
     * Convert the configuration instance into a form that is compatible with xml rpc.
     *
     * @param config    the configuration instance being converted.
     *
     * @return a hashtable representation of the configuration instance
     * @throws Exception on error
     */
    private Hashtable<String, Object> toXmlRpc(Configuration config) throws Exception
    {
        CompositeType type = typeRegistry.getType(config.getClass());
        return type.toXmlRpc(templateOwnerPath, type.unstantiate(config));
    }

    private void updatePathsAndHandles(Configuration config, String path, Hashtable<String, Object> data) throws Exception
    {
        config.setConfigurationPath(path);
        config.setHandle(Long.valueOf(xmlRpcHelper.getConfigHandle(path)));

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
                        Configuration value = (Configuration)collection.get(key);
                        String configPath = basePath + "/" + propertyName + "/" + key;
                        updatePathsAndHandles(value, configPath, (Hashtable<String, Object>)collectionData.get(key));
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

    public void setXmlRpcHelper(XmlRpcHelper xmlRpcHelper)
    {
        this.xmlRpcHelper = xmlRpcHelper;
    }
}
