package com.cinnamonbob.shell;

/**
 * <class-comment/>
 */
public class WindowsShell extends Shell
{
    /**
     * @return
     */
    public String getOpenShellCommand()
    {
        return "cmd.exe";
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
        return "%ERRORLEVEL%";
    }
}
