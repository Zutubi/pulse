package com.zutubi.pulse.dev.sync;

import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.util.config.CompositeConfig;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * A factory for building configured {@link SynchronisePluginsClient} instances.
 */
public class SynchronisePluginsClientFactory extends AbstractClientFactory<SynchronisePluginsClient>
{
    @Override
    protected SynchronisePluginsClient newInstance(File base, CompositeConfig uiConfig, ConsoleUI ui, String[] args)
    {
        return new SynchronisePluginsClient(new DevConfig(base, uiConfig, ui, args), ui);
    }

    @Override
    protected void addExtraOptions(CommandLineConfig switchConfig, Options options)
    {
    }
}
