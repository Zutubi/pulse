package com.zutubi.pulse.master.vfs;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.SlaveProxyFactory;
import com.zutubi.pulse.master.vfs.provider.agent.AgentFileProvider;
import com.zutubi.pulse.master.vfs.provider.local.DefaultLocalFileProvider;
import com.zutubi.pulse.master.vfs.provider.pulse.PulseFileProvider;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.cache.NullFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.springframework.beans.factory.FactoryBean;

public class VfsManagerFactoryBean implements FactoryBean
{
    public static final String FS_AGENT = "agent";
    public static final String FS_LOCAL = "local";
    public static final String FS_PULSE = "pulse";
    public static final String FS_RAM = "ram";

    private ObjectFactory objectFactory;

    private AgentManager agentManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    private DefaultFileSystemManager instance;

    public Object getObject() throws Exception
    {
        synchronized(this)
        {
            if (instance == null)
            {
                instance = new DefaultFileSystemManager();
                instance.setFilesCache(new NullFilesCache());
                instance.addProvider(FS_LOCAL, new DefaultLocalFileProvider());
                instance.addProvider(FS_RAM, new RamFileProvider());
                
                AgentFileProvider agentFileProviderfileProvider = new AgentFileProvider();
                agentFileProviderfileProvider.setAgentManager(agentManager);
                agentFileProviderfileProvider.setSlaveProxyFactory(proxyFactory);
                agentFileProviderfileProvider.setServiceTokenManager(serviceTokenManager);
                instance.addProvider(FS_AGENT, agentFileProviderfileProvider);

                PulseFileProvider pulseFileProvider = objectFactory.buildBean(PulseFileProvider.class);
                instance.addProvider(FS_PULSE, pulseFileProvider);

                instance.init();
            }
        }
        return instance;
    }

    public Class getObjectType()
    {
        return FileSystemManager.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void shutdown()
    {
        if (instance != null)
        {
            instance.close();
        }
    }

    public void setSlaveProxyFactory(SlaveProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    /**
     * Required resource.
     *
     * @param objectFactory instance.
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
