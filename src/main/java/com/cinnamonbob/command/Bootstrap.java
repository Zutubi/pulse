package com.cinnamonbob.command;

import com.cinnamonbob.BobServerProxy;
import com.townleyenterprises.command.CommandOption;
import com.townleyenterprises.command.CommandParser;
import com.townleyenterprises.command.DefaultCommandListener;

/**
 * @author Daniel Ostermeier
 */
public class Bootstrap
{
    private CommandParser parser;

    private CommandOption start = new CommandOption("start",
            's', false, null,
            "start the cinnamonbob build server")
    {
        public void execute() throws Exception
        {
            BobServerProxy server = new BobServerProxy();
            server.start();
        }
    };

    private CommandOption stop = new CommandOption("stop",
            'x',
            false,
            null,
            "stop the cinnamonbob build server")
    {
        public void execute() throws Exception
        {
            BobServerProxy server = new BobServerProxy();
            server.stop();
        }
    };

    private CommandOption build = new CommandOption("build",
            'b',
            true,
            "[ PROJECT-NAME ]",
            "build the specified project")
    {
        public void execute() throws Exception
        {
            BobServerProxy server = new BobServerProxy();
            server.build(getArg());
        }
    };

    public void parse(String args[]) throws Exception
    {
        parser = new CommandParser("bob");
        parser.addCommandListener(new DefaultCommandListener("Start options", new CommandOption[]{start}));
        parser.addCommandListener(new DefaultCommandListener("Stop options", new CommandOption[]{stop}));
        parser.addCommandListener(new DefaultCommandListener("Build options", new CommandOption[]{build}));

        parser.parse(args);

        parser.executeCommands();
    }

    public static void main(String argv[]) throws Exception
    {
        new Bootstrap().parse(argv);
    }
}
