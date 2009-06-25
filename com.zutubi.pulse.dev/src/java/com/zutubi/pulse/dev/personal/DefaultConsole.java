package com.zutubi.pulse.dev.personal;

import com.zutubi.util.io.PasswordReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A default console implementation that sits on the system I/O streams;
 */
public class DefaultConsole implements Console
{
    private BufferedReader inputReader;
    private PasswordReader passwordReader;

    public DefaultConsole()
    {
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        passwordReader = new PasswordReader(inputReader);
    }

    public String readInputLine() throws IOException
    {
        return inputReader.readLine();
    }

    public String readPassword(String prompt, boolean echo)
    {
        return passwordReader.readPassword(prompt, echo);
    }

    public void printOutput(String output)
    {
        System.out.print(output);
    }

    public void printOutputLine(String output)
    {
        System.out.println(output);
    }

    public void printError(String error)
    {
        System.err.print(error);
    }

    public void printErrorLine(String error)
    {
        System.err.println(error);        
    }
}
