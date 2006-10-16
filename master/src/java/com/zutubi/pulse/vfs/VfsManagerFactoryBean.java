package com.zutubi.pulse.vfs;

import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.vfs.local.DefaultLocalFileProvider;
import com.zutubi.pulse.vfs.agent.AgentFileProvider;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.springframework.beans.factory.FactoryBean;

/**
 * <class comment/>
 */
public class VfsManagerFactoryBean implements FactoryBean
{
    private static final Logger LOG = Logger.getLogger(VfsManagerFactoryBean.class);

    private SlaveManager slaveManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    private DefaultFileSystemManager instance;

    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            synchronized(this)
            {
                if (instance == null)
                {
                    instance = new DefaultFileSystemManager();
                    instance.addProvider("local", new DefaultLocalFileProvider());
                    instance.addProvider("ram", new RamFileProvider());

                    AgentFileProvider fileProvider = new AgentFileProvider();
                    fileProvider.setSlaveManager(slaveManager);
                    fileProvider.setSlaveProxyFactory(proxyFactory);
                    fileProvider.setServiceTokenManager(serviceTokenManager);
                    instance.addProvider("agent", fileProvider);

                    instance.init();
                }
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
        instance.close();
    }

    public void setSlaveProxyFactory(SlaveProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
