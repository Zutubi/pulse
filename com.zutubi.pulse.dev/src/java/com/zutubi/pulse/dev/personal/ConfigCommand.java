/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.dev.personal;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.config.DevConfigSetup;
import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.cli.*;

import java.util.*;

/**
 * A command which allows interactive setup of required configuration for the
 * dev tools.
 */
public class ConfigCommand implements Command
{
    private static final Messages I18N = Messages.getInstance(ConfigCommand.class);

    private boolean projectOnly = false;

    @SuppressWarnings({"AccessStaticViaInstance"})
    public int execute(BootContext bootContext) throws Exception
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("project")
                .create('p'));

        CommandLineParser parser = new GnuParser();
        CommandLine commandLine = parser.parse(options, bootContext.getCommandArgv(), false);
        if(commandLine.hasOption('p'))
        {
            projectOnly = true;
        }

        ConsoleUI ui = new ConsoleUI();
        PersonalBuildConfig config = new PersonalBuildConfig(FileSystemUtils.getWorkingDirectory(), new PropertiesConfig(), new Properties(), ui);
        if(!projectOnly)
        {
            DevConfigSetup.setupPulseConfig(ui, config);
        }

        setupLocalConfig(ui, config);
        return 0;
    }

    public void setupLocalConfig(UserInterface ui, PersonalBuildConfig config) throws ClientException
    {
        String pulseProject = ui.inputPrompt(I18N.format("prompt.pulse.project"));

        ui.status("Storing project details in '" + config.getLocalConfigFile().getAbsolutePath() + "'.");
        config.setProperty(PersonalBuildConfig.PROPERTY_PROJECT, pulseProject, true);
    }

    public String getHelp()
    {
        return I18N.format("command.help");
    }

    public String getDetailedHelp()
    {
        return I18N.format("command.detailed.help");
    }

    public List<String> getUsages()
    {
        return Arrays.asList("");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("conf", "configure");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-p [--project]", I18N.format("flag.project"));
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }
}
