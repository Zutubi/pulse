package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.InstallPluginForm;
import com.zutubi.pulse.acceptance.pages.admin.PluginPage;
import com.zutubi.pulse.acceptance.pages.admin.PluginsPage;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.Predicate;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.FileSystemUtils;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions.ACTION_PING;

/**
 * Tests for the plugin management UI.
 */
public class PluginUIAcceptanceTest extends AcceptanceTestBase
{
    private static final String STATE_ENABLED      = "enabled";
    private static final String STATE_DISABLING    = "disabling";
    private static final String STATE_UNINSTALLING = "uninstalling";

    private static final String ID_COMMANDS_CORE = "com.zutubi.pulse.core.commands.core";

    private static final String ACTION_ENABLE    = "enable";
    private static final String ACTION_DISABLE   = "disable";
    private static final String ACTION_UNINSTALL = "uninstall";

    private static final long AGENT_SYNC_TIMEOUT = 120000;
    
    private File tmpDir = null;

    protected void tearDown() throws Exception
    {
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testAllPlugins() throws Exception
    {
        getBrowser().loginAsAdmin();
        PluginsPage pluginsPage = getBrowser().openAndWaitFor(PluginsPage.class);
        assertTrue(pluginsPage.isPluginPresent(ID_COMMANDS_CORE));
        assertFalse(pluginsPage.isActionPresent(ID_COMMANDS_CORE, ACTION_ENABLE));
        assertTrue(pluginsPage.isActionPresent(ID_COMMANDS_CORE, ACTION_DISABLE));
        assertTrue(pluginsPage.isActionPresent(ID_COMMANDS_CORE, ACTION_UNINSTALL));
        assertEquals(STATE_ENABLED, pluginsPage.getPluginState(ID_COMMANDS_CORE));

        PluginPage pluginPage = pluginsPage.clickPlugin(ID_COMMANDS_CORE);
        pluginPage.waitFor();
        assertEquals(STATE_ENABLED, pluginPage.getState());
    }

    public void testInstallPlugin() throws Exception
    {
        final String AGENT_NAME = "localhost";
        
        rpcClient.loginAsAdmin();
        try
        {
            rpcClient.RemoteApi.ensureAgent(AGENT_NAME);

            final String id = getRandomId();
            PluginsPage pluginsPage = installPlugin(id);

            assertTrue(pluginsPage.isPluginPresent(id));
            assertEquals(STATE_ENABLED, pluginsPage.getPluginState(id));

            rpcClient.RemoteApi.doConfigAction(PathUtils.getPath(AGENTS_SCOPE, AGENT_NAME), ACTION_PING);
            TestUtils.waitForCondition(new Condition()
            {
                public boolean satisfied()
                {
                    return agentHasPlugin(id);
                }
            }, AGENT_SYNC_TIMEOUT, "agent to sync plugin");
        }
        finally
        {
            rpcClient.logout();
        }
    }

    private boolean agentHasPlugin(final String id)
    {
        try
        {
            RpcClient agentRpcClient = new RpcClient("http://localhost:" + AcceptanceTestUtils.getAgentPort() + "/xmlrpc");
            Vector<Hashtable<String, Object>> plugins = agentRpcClient.callWithoutToken(RemoteApiClient.API_NAME, "getRunningPlugins", AcceptanceTestUtils.getAgentAdminToken());
            return CollectionUtils.contains(plugins, new Predicate<Hashtable<String, Object>>()
            {
                public boolean satisfied(Hashtable<String, Object> pluginInfo)
                {
                    return id.equals(pluginInfo.get("id"));
                }
            });
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testCancelInstallPlugin() throws Exception
    {
        getBrowser().loginAsAdmin();
        PluginsPage pluginsPage = getBrowser().openAndWaitFor(PluginsPage.class);
        InstallPluginForm form = pluginsPage.clickInstall();
        form.waitFor();
        form.cancel();
        pluginsPage.waitFor();
    }

    public void testDisablePlugin() throws Exception
    {
        String id = getRandomId();
        PluginsPage pluginsPage = installPlugin(id);
        pluginsPage.clickDisable(id);

        getBrowser().waitForElement(By.id(pluginsPage.getActionId(ACTION_ENABLE, id)));

        assertEquals(STATE_DISABLING, pluginsPage.getPluginState(id));
        assertFalse(pluginsPage.isActionPresent(id, ACTION_DISABLE));
        assertFalse(pluginsPage.isActionPresent(id, ACTION_UNINSTALL));

        PluginPage pluginPage = pluginsPage.clickPlugin(id);
        pluginPage.waitFor();
        assertEquals(STATE_DISABLING, pluginPage.getState());
    }

    public void testEnablePlugin() throws Exception
    {
        String id = getRandomId();
        PluginsPage pluginsPage = installPlugin(id);
        pluginsPage.clickDisable(id);

        getBrowser().waitForElement(By.id(pluginsPage.getActionId(ACTION_ENABLE, id)));

        pluginsPage.clickEnable(id);

        getBrowser().waitForElement(By.id(pluginsPage.getActionId(ACTION_DISABLE, id)));

        assertEquals(STATE_ENABLED, pluginsPage.getPluginState(id));
        assertTrue(pluginsPage.isActionPresent(id, ACTION_DISABLE));
        assertTrue(pluginsPage.isActionPresent(id, ACTION_UNINSTALL));

        PluginPage pluginPage = pluginsPage.clickPlugin(id);
        pluginPage.waitFor();
        assertEquals(STATE_ENABLED, pluginPage.getState());
    }

    public void testUninstallPlugin() throws Exception
    {
        String id = getRandomId();
        PluginsPage pluginsPage = installPlugin(id);
        pluginsPage.clickUninstall(id);

        getBrowser().waitForElementToDisappear(By.id(pluginsPage.getActionId(ACTION_UNINSTALL, id)));

        assertEquals(STATE_UNINSTALLING, pluginsPage.getPluginState(id));
        assertFalse(pluginsPage.isActionPresent(id, ACTION_ENABLE));
        assertFalse(pluginsPage.isActionPresent(id, ACTION_DISABLE));
        assertFalse(pluginsPage.isActionPresent(id, ACTION_UNINSTALL));

        PluginPage pluginPage = pluginsPage.clickPlugin(id);
        pluginPage.waitFor();
        assertEquals(STATE_UNINSTALLING, pluginPage.getState());
    }

    private PluginsPage installPlugin(String id) throws IOException
    {
        File testPlugin = makeTestPlugin(id, getRandomProcessorName());

        getBrowser().loginAsAdmin();
        PluginsPage pluginsPage = getBrowser().openAndWaitFor(PluginsPage.class);
        InstallPluginForm form = pluginsPage.clickInstall();
        form.waitFor();
        form.continueFormElements(testPlugin.getAbsolutePath());
        pluginsPage.waitFor();
        return pluginsPage;
    }

    private String getRandomProcessorName()
    {
        return "Test Post-Processor " + random;
    }

    private String getRandomId()
    {
        return AcceptanceTestUtils.PLUGIN_ID_TEST + "." + RandomUtils.insecureRandomString(10);
    }

    private File makeTestPlugin(String id, String name) throws IOException
    {
        tmpDir = FileSystemUtils.createTempDir("PluginUIAcceptanceTest", "");
        return AcceptanceTestUtils.createTestPlugin(tmpDir, id, name);
    }
}
