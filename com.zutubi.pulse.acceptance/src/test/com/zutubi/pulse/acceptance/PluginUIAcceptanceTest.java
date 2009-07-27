package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.InstallPluginForm;
import com.zutubi.pulse.acceptance.pages.admin.PluginPage;
import com.zutubi.pulse.acceptance.pages.admin.PluginsPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * Tests for the plugin management UI.
 */
public class PluginUIAcceptanceTest extends SeleniumTestBase
{
    private static final String STATE_ENABLED      = "enabled";
    private static final String STATE_DISABLING    = "disabling";
    private static final String STATE_UNINSTALLING = "uninstalling";

    private static final String ID_COMMANDS_CORE = "com.zutubi.pulse.core.commands.core";
    private static final String ID_TEST = "com.zutubi.pulse.core.postprocessors.test";

    private static final String ACTION_ENABLE    = "enable";
    private static final String ACTION_DISABLE   = "disable";
    private static final String ACTION_UNINSTALL = "uninstall";

    private File tmpDir = null;

    protected void tearDown() throws Exception
    {
        if(tmpDir != null)
        {
            FileSystemUtils.rmdir(tmpDir);
        }
        
        super.tearDown();
    }

    public void testAllPlugins() throws Exception
    {
        loginAsAdmin();
        PluginsPage pluginsPage = browser.openAndWaitFor(PluginsPage.class);
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
        String id = getRandomId();
        PluginsPage pluginsPage = installPlugin(id);

        assertTrue(pluginsPage.isPluginPresent(id));
        assertEquals(STATE_ENABLED, pluginsPage.getPluginState(id));
    }

    public void testCancelInstallPlugin() throws Exception
    {
        loginAsAdmin();
        PluginsPage pluginsPage = browser.openAndWaitFor(PluginsPage.class);
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

        browser.waitForLocator(pluginsPage.getActionId(ACTION_ENABLE, id));

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

        browser.waitForLocator(pluginsPage.getActionId(ACTION_ENABLE, id));

        pluginsPage.clickEnable(id);

        browser.waitForLocator(pluginsPage.getActionId(ACTION_DISABLE, id));

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

        browser.waitForLocator(pluginsPage.getActionId(ACTION_UNINSTALL, id), true);

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

        loginAsAdmin();
        PluginsPage pluginsPage = browser.openAndWaitFor(PluginsPage.class);
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
        return ID_TEST + "." + random;
    }

    private File makeTestPlugin(String id, String name) throws IOException
    {
        File testPlugin = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.acceptance", "src", "test", "misc", ID_TEST + ".jar"));
        tmpDir = FileSystemUtils.createTempDir("PluginUIAcceptanceTest", "");
        File unzipDir = new File(tmpDir, "unzip");
        PulseZipUtils.extractZip(testPlugin, unzipDir);

        File manifestFile = new File(unzipDir, FileSystemUtils.composeFilename("META-INF", "MANIFEST.MF"));
        String manifest = IOUtils.fileToString(manifestFile);
        manifest = manifest.replaceAll(ID_TEST, id);
        manifest = manifest.replaceAll("Test Post-Processor", name);
        FileSystemUtils.createFile(manifestFile, manifest);

        File pluginFile = new File(tmpDir, id + ".jar");
        PulseZipUtils.createZip(pluginFile, unzipDir, null);
        return pluginFile;
    }
}
