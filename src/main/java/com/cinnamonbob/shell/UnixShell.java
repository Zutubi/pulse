package com.cinnamonbob.shell;

/**
 * <class-comment/>
 */
public class UnixShell extends Shell
{
    /**
     * @return
     */
    public String getOpenShellCommand()
    {
        return System.getenv("SHELL");
    }

    /**
     * @return
     */
    public String getCloseShellCommand()
    {
        return "exit";
    }

    public String getExitStatusVariable()
    {
        return "$?";
    }
}
