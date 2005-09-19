package com.cinnamonbob.shell;

/**
 * <class-comment/>
 */
public class ShellFactory
{
    public static Shell createShell()
    {
        String os = System.getProperty("os.name");
        if (os.equals("Linux"))
        {
            return new UnixShell();
        }
        else if (os.equals("Windows NT"))
        {
            return new WindowsShell();
        }
        else if (os.equals("Windows XP"))
        {
            return new WindowsShell();
        }
        else if (os.equals("Windows 95"))
        {
        }
        return null;
    }
}
