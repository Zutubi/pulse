/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.shell;

import java.util.Properties;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * <class-comment/>
 */
public class NativeShellConfiguration
{
    private static final String OPEN_SHELL_COMMAND = "shell.open";
    private static final String CLOSE_SHELL_COMMAND = "shell.close";
    private static final String ECHO_COMMAND = "command.echo";
    private static final String EXITSTATUS_VARIABLE = "variable.exitstatus";

    private Properties props = null;

    /**
     *
     */
    public String getOpenShellCommand()
    {
//        return System.getenv("SHELL");
        return getProperties().getProperty(OPEN_SHELL_COMMAND);
    }

    /**
     *
     */
    public String getCloseShellCommand()
    {
        return getProperties().getProperty(CLOSE_SHELL_COMMAND);
    }

    /**
     *
     */
    public String getExitStatusVariable()
    {
        return MessageFormat.format(getProperties().getProperty("variable"), getProperties().getProperty(EXITSTATUS_VARIABLE));
    }

    /**
     *
     */
    public String getEchoCommand()
    {
        return getProperties().getProperty(ECHO_COMMAND);
    }

    public String getPathVariable()
    {
        return getProperties().getProperty("variable.path");
    }

    private Properties getProperties()
    {
        if (props == null)
        {
            String os = System.getProperty("os.name").toLowerCase();
            props = new Properties();
            try
            {
                props.load(getClass().getResourceAsStream(os.replace(' ', '.').replace("/", "") + ".properties"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return props;
    }

}
