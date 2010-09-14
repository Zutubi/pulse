package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.util.config.CompositeConfig;
import org.apache.commons.cli.Options;

import java.io.File;

/**
 * A factory for building configured {@link PersonalBuildClient} instances.
 */
public class PersonalBuildClientFactory extends AbstractClientFactory<PersonalBuildClient>
{
    @Override
    protected PersonalBuildClient newInstance(File base, CompositeConfig uiConfig, ConsoleUI ui, String[] args)
    {
        PersonalBuildConfig config = new PersonalBuildConfig(base, uiConfig, ui, args);
        return new PersonalBuildClient(config, ui);
    }

    @Override
    protected void addExtraOptions(CommandLineConfig switchConfig, Options options)
    {
        addPropertyOption(switchConfig, options, 'r', "project", PersonalBuildConfig.PROPERTY_PROJECT);
        addPropertyOption(switchConfig, options, 'e', "revision", PersonalBuildConfig.PROPERTY_REVISION);
        addPropertyOption(switchConfig, options, 'f', "file", PersonalBuildConfig.PROPERTY_PATCH_FILE);
        addPropertyOption(switchConfig, options, 't', "patch-type", PersonalBuildConfig.PROPERTY_PATCH_TYPE);

        addBooleanOption(switchConfig, options, "send-request", PersonalBuildConfig.PROPERTY_SEND_REQUEST);
        addBooleanOption(switchConfig, options, "update", PersonalBuildConfig.PROPERTY_UPDATE);
    }
}
