package com.zutubi.pulse.master.vfs.provider.agent;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.SlaveProxyFactory;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <class comment/>
 */
public class AgentFileSystem extends AbstractFileSystem
{
    private static final Logger LOG = Logger.getLogger(AgentFileSystem.class);

    private AgentManager agentManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    private List<FileName> rootNames;
    private String agent;
    private String osName;

    public AgentFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, parentLayer, fileSystemOptions);

        agent = ((AgentFileName)rootName).getAddress();
    }

    public void init() throws FileSystemException
    {
        super.init();

        SlaveService slaveService = getProxy();
        String token = getToken();
        try
        {
            getOs(slaveService, token);
            loadFileSystemRoots(slaveService, token);
        }
        catch (HessianRuntimeException e)
        {
            LOG.warning(e);
            throw new FileSystemException(e);
        }
    }

    protected FileObject createFile(final FileName name) throws Exception
    {
        return new AgentFileObject(name, this);
    }

    protected void addCapabilities(Collection caps)
    {
        caps.addAll(AgentFileProvider.CAPABILITIES);
    }

    private void getOs(SlaveService slaveService, String token)
    {
        SystemInfo info = slaveService.getSystemInfo(token);
        osName = (String) info.getSystemProperties().get("os.name");
    }

    private void loadFileSystemRoots(SlaveService slaveService, String token) throws FileSystemException
    {
        rootNames = new LinkedList<FileName>();
        for (String root : slaveService.listRoots(token))
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
        Agent agent = agentManager.getAgent(Long.valueOf(this.agent));
        return proxyFactory.createProxy(agent.getConfig());
    }

    protected String getToken()
    {
        return serviceTokenManager.getToken();
    }

    public boolean isWindows()
    {
        return osName != null && osName.toLowerCase().startsWith("win");
    }

    public void setProxyFactory(SlaveProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
