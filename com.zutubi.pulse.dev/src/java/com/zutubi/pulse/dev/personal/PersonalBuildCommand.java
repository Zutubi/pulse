package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.personal.PersonalBuildException;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.util.config.CommandLineConfig;
import com.zutubi.pulse.dev.bootstrap.DevBootstrapManager;
import com.zutubi.util.Pair;
import com.zutubi.util.config.CompositeConfig;
import com.zutubi.util.config.PropertiesConfig;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 */
@SuppressWarnings({ "AccessStaticViaInstance" })
public class PersonalBuildCommand implements Command
{
    private File base;
    private String[] files = {};
    private CommandLineConfig switchConfig;
    private PropertiesConfig defineConfig;
    private String patchFilename;
    private boolean noRequest = false;
    private boolean statusOnly = false;
    private ConsoleUI console = new ConsoleUI();

    private void processArguments(String... argv) throws ParseException
    {
        switchConfig = new CommandLineConfig();
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("quiet")
                .create('q'));
        options.addOption(OptionBuilder.withLongOpt("verbose")
                .create('v'));
        options.addOption(OptionBuilder.withLongOpt("status")
                .create('t'));
        options.addOption(OptionBuilder.withLongOpt("define")
                .hasArg()
                .create('d'));
        options.addOption(OptionBuilder.withLongOpt("file")
                .hasArg()
                .create('f'));
        options.addOption(OptionBuilder.withLongOpt("no-request")
                .create('n'));
        options.addOption(OptionBuilder.withLongOpt("base-dir")
                .hasArg()
                .create('b'));

        addPropertyOption(options, 's', "server", PersonalBuildConfig.PROPERTY_PULSE_URL);
        addPropertyOption(options, 'u', "user", PersonalBuildConfig.PROPERTY_PULSE_USER);
        addPropertyOption(options, 'p', "password", PersonalBuildConfig.PROPERTY_PULSE_PASSWORD);
        addPropertyOption(options, 'r', "project", PersonalBuildConfig.PROPERTY_PROJECT);

        CommandLineParser parser = new PosixParser();

        CommandLine commandLine = parser.parse(options, argv, true);
        Properties defines = new Properties();

        if (commandLine.hasOption('d'))
        {
            addDefinedOption(defines, commandLine.getOptionValue('d'));
        }
        if (commandLine.hasOption('q'))
        {
            console.setVerbosity(ConsoleUI.Verbosity.QUIET);
        }
        if (commandLine.hasOption('v'))
        {
            console.setVerbosity(ConsoleUI.Verbosity.VERBOSE);
        }
        if (commandLine.hasOption('t'))
        {
            statusOnly = true;
        }
        if (commandLine.hasOption('n'))
        {
            noRequest = true;
        }
        if(commandLine.hasOption('f'))
        {
            patchFilename = commandLine.getOptionValue('f');
        }

        if(commandLine.hasOption('b'))
        {
            String baseName = commandLine.getOptionValue('b');
            base = new File(baseName);
            if (!base.isDirectory())
            {
                System.err.println("Base directory specified '" + baseName + "' is not a directory");
                System.exit(1);
            }
        }
        else
        {
            base = new File(System.getProperty("user.dir"));
        }

        switchConfig.setCommandLine(commandLine);
        defineConfig = new PropertiesConfig(defines);
        files = commandLine.getArgs();
    }

    private void addDefinedOption(Properties defines, String value) throws ParseException
    {
        int index = value.indexOf('=');
        if (index <= 0 || index >= value.length() - 1)
        {
            throw new ParseException("Invalid property definition syntax '" + value + "' (expected name=value)");
        }

        String propertyName = value.substring(0, index);
        String propertyValue = value.substring(index + 1);

        defines.put(propertyName, propertyValue);
    }

    private void addPropertyOption(Options options, char shortOption, String longOption, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(longOption)
                .hasArg()
                .create(shortOption));
        switchConfig.mapSwitch(Character.toString(shortOption), property);
    }

    public int execute(PersonalBuildClient client)
    {
        try
        {
            Pair<WorkingCopy,WorkingCopyContext> pair = client.checkConfiguration();

            File patchFile;

            if(patchFilename == null)
            {
                try
                {
                    patchFile = File.createTempFile("pulse.patch.", ".zip");
                    patchFile.deleteOnExit();
                }
                catch (IOException e)
                {
                    console.error("Unable to create temporary patch file: " + e.getMessage(), e);
                    return 1;
                }
            }
            else
            {
                patchFile = new File(patchFilename);
            }

            Revision revision = client.preparePatch(pair.first, pair.second, patchFile, files);
            if (revision != null && !noRequest)
            {
                client.sendRequest(revision, patchFile);
            }
        }
        catch (UserAbortException e)
        {
            return 2;
        }
        catch (PersonalBuildException e)
        {
            console.error(e.getMessage(), e);
            return 1;
        }

        return 0;
    }

    private int execute(String[] argv) throws ParseException
    {
        processArguments(argv);

        DevBootstrapManager.startup("com/zutubi/pulse/dev/personal/bootstrap/context/applicationContext.xml");
        try
        {
            CompositeConfig uiConfig = new CompositeConfig(switchConfig, defineConfig);
            PersonalBuildConfig config = new PersonalBuildConfig(base, uiConfig);
            PersonalBuildClient client = new PersonalBuildClient(config, console);

            return execute(client);
        }
        finally
        {
            DevBootstrapManager.shutdown();
        }
    }

    public int execute(BootContext context) throws ParseException
    {
        return execute(context.getCommandArgv());
    }

    public String getHelp()
    {
        return "request a personal build";
    }

    public String getDetailedHelp()
    {
        return "Sends a personal build request to a pulse server.  This involves updating\n" +
               "the current working copy, analysing any outstanding changes, forming a patch\n" +
               "file and sending the patch to the pulse server to execute a build.\n\n" +
               "Configuration is defined via properties files or command line arguments.  The\n" +
               "configuration specifies connection details for the pulse server, along with\n" +
               "information about the project you wish to execute.  The SCM configuration of\n" +
               "the project must match the working copy.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList("", "<file> ...", ":<changelist>");
    }

    public List<String> getAliases()
    {
        return Arrays.asList("pe", "per", "pers");
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-r [--project] project", "set project to build");
        options.put("-s [--server] url", "set pulse server url");
        options.put("-u [--user] name", "set pulse user name");
        options.put("-p [--password] password", "set pulse password");
        options.put("-b [--base-dir] dir", "set base directory of working copy");
        options.put("-f [--file] filename", "set patch file name");
        options.put("-d [--define] name=value", "set named property to given value");
        options.put("-q [--quiet]", "suppress unnecessary output");
        options.put("-v [--verbose]", "show verbose output");
        options.put("-n [--no-request]", "create patch but do not request build");
        options.put("-t [--status]", "show status only, do not update or build");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    public static void main(String[] argv)
    {
        PersonalBuildCommand command = new PersonalBuildCommand();
        try
        {
            System.exit(command.execute(argv));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
