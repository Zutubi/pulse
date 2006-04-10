package com.zutubi.pulse.command;

import com.zutubi.pulse.bootstrap.SystemBootstrapManager;
import com.zutubi.pulse.util.logging.Logger;


/**
 *
 */
public class Bootstrap
{
    private static final Logger LOG = Logger.getLogger(Bootstrap.class);

    //TODO: support command the form ... options COMMAND command-options ARGUMENTS

    public void parse(String args[]) throws Exception
    {
        if (args.length == 0)
        {
            return;
        }

        //TODO: support configuration of host and port settings.
        String command = args[0];
        if ("start".equals(command))
        {
            start(args);
        }
        else
        {
            LOG.warning("Unrecognised command '" + command + "'");
        }
    }

    public static void main(String argv[]) throws Exception
    {
        new Bootstrap().parse(argv);
    }

    public void start(String argv[]) throws Exception
    {
        SystemBootstrapManager bootstrap = new SystemBootstrapManager();
        bootstrap.bootstrapSystem();
    }
}
