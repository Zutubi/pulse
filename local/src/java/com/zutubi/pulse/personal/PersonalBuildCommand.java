package com.zutubi.pulse.personal;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.config.CommandLineConfig;
import com.zutubi.pulse.scm.WorkingCopy;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
@SuppressWarnings({"AccessStaticViaInstance"})
public class PersonalBuildCommand implements Command, PersonalBuildUI
{
    private File base;
    private String[] files;
    private CommandLineConfig uiConfig;
    private BufferedReader inputReader;
    private Verbosity verbosity;

    public void processArguments(String... argv) throws ParseException
    {
        uiConfig = new CommandLineConfig();
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("quiet")
                .create('q'));
        options.addOption(OptionBuilder.withLongOpt("verbose")
                .create('v'));

        addPropertyOption(options, 's', "server", PersonalBuildConfig.PROPERTY_PULSE_URL);
        addPropertyOption(options, 'u', "user", PersonalBuildConfig.PROPERTY_PULSE_USER);
        addPropertyOption(options, 'p', "password", PersonalBuildConfig.PROPERTY_PULSE_PASSWORD);
        addPropertyOption(options, 'r', "project", PersonalBuildConfig.PROPERTY_PROJECT);
        addPropertyOption(options, 'b', "specification", PersonalBuildConfig.PROPERTY_SPECIFICATION);

        CommandLineParser parser = new PosixParser();

        CommandLine commandLine = parser.parse(options, argv, true);

        if(commandLine.hasOption('q'))
        {
            setVerbosity(Verbosity.QUIET);
        }
        if(commandLine.hasOption('v'))
        {
            setVerbosity(Verbosity.VERBOSE);
        }

        uiConfig.setCommandLine(commandLine);
        base = new File(System.getProperty("user.dir"));
        files = commandLine.getArgs();
    }

    private void addPropertyOption(Options options, char shortOption, String longOption, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(longOption)
                .hasArg()
                .create(shortOption));
        uiConfig.mapSwitch(Character.toString(shortOption), property);
    }

    private int execute(String[] argv) throws ParseException
    {
        processArguments(argv);
        inputReader = new BufferedReader(new InputStreamReader(System.in));

        PersonalBuildConfig config = new PersonalBuildConfig(base, uiConfig);
        PersonalBuildClient client = new PersonalBuildClient(config);
        client.setUI(this);

        try
        {
            WorkingCopy wc = client.checkConfiguration();
            File patchFile = null;

            try
            {
                patchFile = File.createTempFile("pulse.patch", ".zip");
            }
            catch (IOException e)
            {
                error("Unable to create temporary patch file: " + e.getMessage(), e);
                return 1;
            }

            patchFile.deleteOnExit();

            PatchArchive patch = client.preparePatch(wc, patchFile);
            client.sendRequest(patch);
        }
        catch(UserAbortException e)
        {
            return 2;
        }
        catch(PersonalBuildException e)
        {
            error(e.getMessage(), e);
            return 1;
        }

        return 0;
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
        return "Sends a personal build request to a pulse server.  This involves taking a\n" +
               "snapshot of the current working directory state, forming a pulse patch file\n" +
               "and sending the patch to the pulse server to execute a build.  If the working\n" +
               "copy is out of date, you will be prompted to update it before a patch will be\n" +
               "constructed.\n\n" +
               "Configuration is defined via properties files or command line arguments.  The\n" +
               "configuration specifies connection details for the pulse server, along with\n" +
               "information about the project and build specification you wish to execute.\n" +
               "The SCM configuration of the project must match the working copy.";
    }

    public List<String> getUsages()
    {
        return Arrays.asList(new String[] { "" });
    }

    public List<String> getAliases()
    {
        return Arrays.asList(new String[] { "pe", "per", "pers" });
    }

    public Map<String, String> getOptions()
    {
        Map<String, String> options = new LinkedHashMap<String, String>();
        options.put("-r [--project] project", "set project to build");
        options.put("-b [--specification] spec", "set build specification to build");
        options.put("-s [--server] url", "set pulse server url");
        options.put("-u [--user] name", "set pulse user name");
        options.put("-p [--password] password", "set pulse password");
        options.put("-q [--quiet]", "suppress unnecessary output");
        options.put("-v [--verbose]", "show verbose output");
        return options;
    }

    public boolean isDefault()
    {
        return false;
    }

    private void fatal(String message)
    {
        System.err.println("Error: " + message);
        System.exit(1);
    }

    public void setVerbosity(Verbosity verbosity)
    {
        this.verbosity = verbosity;
    }

    public void status(String message)
    {
        if(verbosity != Verbosity.QUIET)
        {
            System.out.println(message);
        }
    }

    public void warning(String message)
    {
        System.out.println("Warning: " + message);
    }

    public void error(String message)
    {
        System.out.println("Error: " + message);
    }

    public void error(String message, Throwable throwable)
    {
        if(verbosity == Verbosity.VERBOSE)
        {
            throwable.printStackTrace(System.err);
        }

        error(message);
    }

    public Response ynaPrompt(String question, Response defaultResponse)
    {
        String choices = "Yes/No/Always";

        switch(defaultResponse)
        {
            case YES:
                choices += " [default: Yes]";
                break;
            case NO:
                choices += " [default: No]";
                break;
            case ALWAYS:
                choices += " [default: Always]";
                break;
        }

        try
        {
            System.out.println(question);

            Response response = null;
            while(response == null)
            {
                System.out.print(choices + "> ");
                String input = inputReader.readLine();
                response = Response.fromInput(input, defaultResponse);
            }

            return response;
        }
        catch (IOException e)
        {
            fatal("Unable to prompt for input: " + e.getMessage());
            return null;
        }
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
