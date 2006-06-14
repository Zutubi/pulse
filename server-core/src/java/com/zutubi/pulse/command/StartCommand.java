package com.zutubi.pulse.command;

import org.apache.commons.cli.*;
import com.zutubi.pulse.bootstrap.SystemBootstrapManager;

/**
 * The start command is used as the entry point to starting the system.
 *
 * @author Daniel Ostermeier
 */
public class StartCommand implements Command
{
    public void parse(String argv[]) throws ParseException
    {
        Options options = new Options();
        CommandLineParser parser = new PosixParser();
        parser.parse(options, argv, true);
    }

    public int execute()
    {
        try
        {
            SystemBootstrapManager bootstrap = new SystemBootstrapManager();
            bootstrap.bootstrapSystem();
            return 0;
        }
        catch (Exception e)
        {
            return 1;
        }
    }

    public static void main(String[] args) throws Exception
    {
        Command cmd = new StartCommand();
        cmd.parse(args);
        cmd.execute();
    }
}
