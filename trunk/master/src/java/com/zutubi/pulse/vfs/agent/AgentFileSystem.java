package com.zutubi.pulse.vfs.agent;

import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.ServiceTokenManager;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class AgentFileSystem extends AbstractFileSystem
{
    private SlaveManager slaveManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    private List<FileName> rootNames;
    private String agent;

    public AgentFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, parentLayer, fileSystemOptions);

        agent = ((AgentFileName)rootName).getAddress();
    }

    public void init() throws FileSystemException
    {
        super.init();

        loadFileSystemRoots();
    }

    protected FileObject createFile(final FileName name) throws Exception
    {
        return new AgentFileObject(name, this);
    }

    protected void addCapabilities(Collection caps)
    {
        caps.addAll(AgentFileProvider.CAPABILITIES);
    }

    private void loadFileSystemRoots() throws FileSystemException
    {
        rootNames = new LinkedList<FileName>();
        for (String root : getProxy().listRoots(getToken()))
        {
            // munge these roots.
            rootNames.add(getFileSystemManager().resolveName(getRootName(), root));
        }
    }

    protected String[] getRoots()
    {
        List<String> roots = new LinkedList<String>();
        for (FileName rootName : rootNames)
        {
            roots.add(rootName.getPath());
        }
        return roots.toArray(new String[roots.size()]);
    }

    protected boolean isRoot(String name)
    {
        try
        {
            FileName rootName = getFileSystemManager().resolveName(getRootName(), name);
            if (rootNames.contains(rootName))
            {
                return true;
            }
        }
        catch (FileSystemException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    protected SlaveService getProxy() throws FileSystemException
    {
        try
        {
            Slave slave = slaveManager.getSlave(Long.valueOf(agent));
            return proxyFactory.createProxy(slave);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new FileSystemException(e);
        }
    }

    protected String getToken()
    {
        return serviceTokenManager.getToken();
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setProxyFactory(SlaveProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
