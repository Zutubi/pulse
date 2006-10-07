package com.zutubi.pulse.personal;

import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.config.CommandLineConfig;
import com.zutubi.pulse.scm.WorkingCopy;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public void parse(String... argv) throws Exception
    {
        uiConfig = new CommandLineConfig();
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
                .withDescription("display usage")
                .create('h'));
        options.addOption(OptionBuilder.withLongOpt("quiet")
                .withDescription("suppress unnecessary output")
                .create('q'));
        options.addOption(OptionBuilder.withLongOpt("verbose")
                .withDescription("show verbose output")
                .create('v'));

        addPropertyOption(options, 's', "server", "set pulse Server url", PersonalBuildConfig.PROPERTY_PULSE_URL);
        addPropertyOption(options, 'u', "user", "set pulse User name", PersonalBuildConfig.PROPERTY_PULSE_USER);
        addPropertyOption(options, 'p', "password", "set pulse Password", PersonalBuildConfig.PROPERTY_PULSE_PASSWORD);
        addPropertyOption(options, 'r', "project", "set pulse pRoject", PersonalBuildConfig.PROPERTY_PROJECT);
        addPropertyOption(options, 'b', "specification", "set pulse Build specification", PersonalBuildConfig.PROPERTY_SPECIFICATION);

        CommandLineParser parser = new PosixParser();

        CommandLine commandLine = parser.parse(options, argv, true);
        if(commandLine.hasOption('h'))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("pulse-personal", options);
            System.exit(0);
        }

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

    private void addPropertyOption(Options options, char shortOption, String longOption, String description, String property)
    {
        options.addOption(OptionBuilder.withLongOpt(longOption)
                .withDescription(description)
                .create(shortOption));
        uiConfig.mapSwitch(Character.toString(shortOption), property);
    }

    public int execute()
    {
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

    public String getHelp()
    {
        return "request a personal build";
    }

    private void fatal(String message)
    {
        System.err.println("Error: " + message);
        System.exit(1);
    }

    public static void main(String[] argv)
    {
        PersonalBuildCommand command = new PersonalBuildCommand();
        try
        {
            command.parse(argv);
            System.exit(command.execute());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
}
