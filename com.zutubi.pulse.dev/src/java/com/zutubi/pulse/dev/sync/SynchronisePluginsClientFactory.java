package com.zutubi.pulse.dev.sync;

import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.util.config.CompositeConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * A factory for building configured {@link SynchronisePluginsClient} instances.
 */
public class SynchronisePluginsClientFactory extends AbstractClientFactory<SynchronisePluginsClient>
{
    @Override
    protected SynchronisePluginsClient newInstance(File base, CompositeConfig uiConfig, ConsoleUI ui, CommandLine commandLine)
    {
        SynchronisePluginsClient client = new SynchronisePluginsClient(new DevConfig(base, uiConfig, ui, commandLine.getArgs()), ui);
        client.setPluginManager(SpringComponentContext.<PluginManager>getBean("pluginManager"));
        client.setPluginSynchroniser(SpringComponentContext.<PluginSynchroniser>getBean("pluginSynchroniser"));
        return client;
    }

    @Override
    protected void addExtraOptions(CommandLineConfig switchConfig, Options options)
    {
    }
}
