package com.zutubi.pulse.master.vfs.provider.agent;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.SlaveProxyFactory;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.SystemUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.mockito.Matchers;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;


public class AgentFileSystemTest extends PulseTestCase
{
    private DefaultFileSystemManager fileSystemManager;
    private AgentManager agentManager;
    private SlaveProxyFactory proxyFactory;
    private ServiceTokenManager serviceTokenManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        agentManager = mock(AgentManager.class);
        proxyFactory = mock(SlaveProxyFactory.class);
        serviceTokenManager = mock(ServiceTokenManager.class);

        AgentFileProvider fileProvider = new AgentFileProvider();
        fileProvider.setAgentManager(agentManager);
        fileProvider.setSlaveProxyFactory(proxyFactory);
        fileProvider.setServiceTokenManager(serviceTokenManager);

        fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.addProvider("agent", fileProvider);
        fileSystemManager.init();
    }

    public void testSimpleListingOfAgentFileSystem() throws FileSystemException
    {
        Agent agent = mock(Agent.class);
        stub(agent.getConfig()).toReturn(null);

        SystemInfo sysInfo = mock(SystemInfo.class);
        stub(sysInfo.getSystemProperties()).toReturn(System.getProperties());

        SlaveService proxy = mock(SlaveService.class);
        stub(proxy.getSystemInfo(Matchers.anyString())).toReturn(sysInfo);
        File base = new File(".");
        String[] listRoots = new String[base.listFiles().length];
        CollectionUtils.mapToArray(base.listFiles(), new Mapping<File, String>()
        {
            public String map(File file)
            {
                return file.getName();
            }
        }, listRoots);
        stub(proxy.listRoots(null)).toReturn(listRoots);
        stub(proxy.getFileInfo(Matchers.anyString(), Matchers.anyString())).toAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invoc) throws Throwable
            {
                return new FileInfo(new File((String)invoc.getArguments()[1]));
            }
        });
        
        stub(agentManager.getAgent(1234)).toReturn(agent);
        stub(proxyFactory.createProxy(agent.getConfig())).toReturn(proxy);

        FileObject fo = fileSystemManager.resolveFile("agent://1234");

        if (SystemUtils.IS_WINDOWS)
        {
            assertEquals(listRoots.length,  fo.getChildren().length);
        }
    }
}
