package com.zutubi.pulse.personal;

import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.PulseException;
import org.apache.commons.cli.*;

import java.io.File;

/**
 */
public class PersonalBuildCommand implements Command
{
    private File base;
    private String[] files;

    @SuppressWarnings({"ACCESS_STATIC_VIA_INSTANCE"})
    public void parse(String... argv) throws Exception
    {
        Options options = new Options();

        options.addOption(OptionBuilder.withLongOpt("help")
                .withDescription("display usage")
                .create('h'));

        CommandLineParser parser = new PosixParser();

        CommandLine commandLine = parser.parse(options, argv, true);
        if(commandLine.hasOption('h'))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("pulse-personal", options);
            System.exit(0);
        }

        base = new File(System.getProperty("user.dir"));
        files = commandLine.getArgs();
    }

    public int execute()
    {
        PersonalBuildConfig config = new PersonalBuildConfig(base);
        PersonalBuildClient client = new PersonalBuildClient(config);

        File patchFile = new File("pulse.patch");
        if(patchFile.exists())
        {
            System.err.println("Patch file exists: I'm gonna nuke it");
            patchFile.delete();
        }

        try
        {
            PatchArchive patch = new PatchArchive(base, patchFile);
            client.sendRequest(patch);
        }
        catch (PulseException e)
        {
            e.printStackTrace();
            fatal(e.getMessage());
        }

        return 0;
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
}
