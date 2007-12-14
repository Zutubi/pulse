package com.zutubi.pulse.plugins;

import com.zutubi.pulse.core.plugins.CommandExtensionManager;

/**
 *
 *
 */
public class PluginExtensionManagerTest extends BasePluginSystemTestCase
{
    //TODO: move these extension manager tests into a more appropriate location.
    public void testCommandExtensionManager() throws Exception
    {
        installPulseInternalBundles();

        //TODO: install a couple of command bundles so this does some actual work.

        startupPluginCore();

        // load all of the relevant internal extension point managers.
        CommandExtensionManager extensionManager = new CommandExtensionManager();
        extensionManager.setPluginManager(manager);
        extensionManager.init();

        // initialise the extension system
        manager.initialiseExtensions();
    }

    public void testConfigurationExtensionManager() throws Exception
    {
        // install and load a configuration bundle.
        // validate the correct installation of the bundle into the configuration system.
    }

    public void testScmExtensionManager() throws Exception
    {
        // install and load an scm bundle.
        // validate the correct installation of the bundle into the scm extension system.
    }
}
