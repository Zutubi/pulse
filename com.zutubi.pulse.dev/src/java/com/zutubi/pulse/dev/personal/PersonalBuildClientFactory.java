package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.client.AbstractClientFactory;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.pulse.dev.util.OptionUtils;
import com.zutubi.util.config.CompositeConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.Properties;

/**
 * A factory for building configured {@link PersonalBuildClient} instances.
 */
public class PersonalBuildClientFactory extends AbstractClientFactory<PersonalBuildClient>
{
    @Override
    protected PersonalBuildClient newInstance(File base, CompositeConfig uiConfig, ConsoleUI ui, CommandLine commandLine) throws ParseException
    {
        PersonalBuildConfig config = new PersonalBuildConfig(base, uiConfig, processOverrides(commandLine), ui, commandLine.getArgs());
        return new PersonalBuildClient(config, ui);
    }

    @Override
    @SuppressWarnings({"AccessStaticViaInstance"})
    protected void addExtraOptions(CommandLineConfig switchConfig, Options options)
    {
        options.addOption(OptionBuilder.withLongOpt("override")
                .hasArg()
                .create('o'));

        addPropertyOption(switchConfig, options, 'r', "project", PersonalBuildConfig.PROPERTY_PROJECT);
        addPropertyOption(switchConfig, options, 'e', "revision", PersonalBuildConfig.PROPERTY_REVISION);
        addPropertyOption(switchConfig, options, 'f', "file", PersonalBuildConfig.PROPERTY_PATCH_FILE);
        addPropertyOption(switchConfig, options, 't', "patch-type", PersonalBuildConfig.PROPERTY_PATCH_TYPE);

        addBooleanOption(switchConfig, options, "send-request", PersonalBuildConfig.PROPERTY_SEND_REQUEST);
        addBooleanOption(switchConfig, options, "update", PersonalBuildConfig.PROPERTY_UPDATE);
    }

    protected Properties processOverrides(CommandLine commandLine) throws ParseException
    {
        Properties overrides = new Properties();
        if (commandLine.hasOption('o'))
        {
            String[] values = commandLine.getOptionValues('o');
            for (String value: values)
            {
                OptionUtils.addDefinedOption(value, overrides);
            }
        }

        return overrides;
    }
}
