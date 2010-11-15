package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.util.*;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class PluginRepositoryAcceptanceTest extends AcceptanceTestBase
{
    private static final String ID_CORE_COMMANDS = "com.zutubi.pulse.core.commands.core";
    private static final String ID_EMAIL_HOOK_TASK = "com.zutubi.pulse.master.hook.email";

    private HttpPluginRepository repository;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        repository = new HttpPluginRepository(baseUrl + "/pluginrepository/");
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testListAvailable() throws IOException
    {
        List<PluginInfo> infoList = repository.getAvailablePlugins(PluginRepository.Scope.MASTER);
        assertTrue(isPluginListed(infoList, ID_CORE_COMMANDS));
        assertTrue(isPluginListed(infoList, ID_EMAIL_HOOK_TASK));

        infoList = repository.getAvailablePlugins(PluginRepository.Scope.CORE);
        assertTrue(isPluginListed(infoList, ID_CORE_COMMANDS));
        assertFalse(isPluginListed(infoList, ID_EMAIL_HOOK_TASK));
    }

    private boolean isPluginListed(List<PluginInfo> infoList, final String id)
    {
        return CollectionUtils.contains(infoList, new Predicate<PluginInfo>()
        {
            public boolean satisfied(PluginInfo pluginInfo)
            {
                return pluginInfo.getId().equals(id);
            }
        });
    }

    public void testDownloadPlugin() throws Exception
    {
        File tmpDir = FileSystemUtils.createTempDir(getName(), ".tmp");
        try
        {
            String random = RandomUtils.randomString(10);
            String id = AcceptanceTestUtils.PLUGIN_ID_TEST + "." + random;
            File pluginJar = AcceptanceTestUtils.createTestPlugin(tmpDir, id, getName() + "-" + random);
            xmlRpcHelper.installPlugin(pluginJar.getAbsolutePath());
            
            File downloadedJar = downloadPlugin(tmpDir, id, "2.0.0");
            IOAssertions.assertFilesEqual(pluginJar, downloadedJar);
        }
        finally
        {
            FileSystemUtils.rmdir(tmpDir);
        }
    }

    private File downloadPlugin(File tmpDir, String id, String version) throws IOException
    {
        File jar = new File(tmpDir, id + "-" + version + "-downloaded.jar");
        URI uri = repository.getPluginLocation(new PluginInfo(id, version, PluginRepository.Scope.CORE));
        GetMethod getMethod = AcceptanceTestUtils.httpGet(uri.toString(), null);
        try
        {
            FileSystemUtils.createFile(jar, getMethod.getResponseBodyAsStream());
            return jar;
        }
        finally
        {
            getMethod.releaseConnection();
        }
    }
}
