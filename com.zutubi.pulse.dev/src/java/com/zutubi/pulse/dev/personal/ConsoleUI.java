package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.util.io.PasswordReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 */
public class ConsoleUI implements PersonalBuildUI
{
    private static final String ECHO_PROPERTY = "pulse.echo.passwords";

    public enum Verbosity
    {
        QUIET,
        NORMAL,
        VERBOSE
    }
    private BufferedReader inputReader;
    private PasswordReader passwordReader;
    private Verbosity verbosity;
    private String indent = "";

    public ConsoleUI()
    {
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        passwordReader = new PasswordReader(inputReader);
    }

    private void fatal(String message)
    {
        print("Error: " + message, System.err);
        System.exit(1);
    }

    public void setVerbosity(Verbosity verbosity)
    {
        this.verbosity = verbosity;
    }

    public boolean isDebugEnabled()
    {
        return verbosity == Verbosity.VERBOSE;
    }

    public void debug(String message)
    {
        if (verbosity == Verbosity.VERBOSE)
        {
            print(message, System.out);
        }
    }

    public void status(String message)
    {
        if (verbosity != Verbosity.QUIET)
        {
            print(message, System.out);
        }
    }

    public void warning(String message)
    {
        print("Warning: " + message, System.err);
    }

    public void error(String message)
    {
        print("Error: " + message, System.err);
    }

    public void error(String message, Throwable throwable)
    {
        if (verbosity == Verbosity.VERBOSE)
        {
            throwable.printStackTrace(System.err);
        }

        error(message);
    }

    private void print(String message, PrintStream stream)
    {
        stream.println(indent + message);
    }

    public void enterContext()
    {
        indent += "  ";
    }

    public void exitContext()
    {
        if(indent.length() >= 2)
        {
            indent = indent.substring(2);
        }
    }

    public String inputPrompt(String question)
    {
        System.out.print(question + ": ");
        try
        {
            return inputReader.readLine();
        }
        catch (IOException e)
        {
            fatal("Unable to prompt for input: " + e.getMessage());
            return null;
        }
    }

    public String inputPrompt(String prompt, String defaultResponse)
    {
        System.out.print(prompt + " [default: " + defaultResponse + "]: ");
        try
        {
            String response = inputReader.readLine();
            if(response.length() == 0)
            {
                response = defaultResponse;
            }

            return response;
        }
        catch (IOException e)
        {
            fatal("Unable to prompt for input: " + e.getMessage());
            return defaultResponse;
        }
    }

    public String passwordPrompt(String question)
    {
        String result = passwordReader.readPassword(question + ": ", Boolean.getBoolean(ECHO_PROPERTY));
        if (result == null)
        {
            fatal("Unable to prompt for password");
        }

        return result;
    }

    public Response ynPrompt(String question, Response defaultResponse)
    {
        String choices = "Yes/No";

        switch (defaultResponse)
        {
            case YES:
                choices += " [default: Yes]";
                break;
            case NO:
                choices += " [default: No]";
                break;
        }

        try
        {
            System.out.println(question);

            Response response = null;
            while (response == null)
            {
                System.out.print(choices + "> ");
                String input = inputReader.readLine();
                response = Response.fromInput(input, defaultResponse, Response.YES, Response.NO);
            }

            return response;
        }
        catch (IOException e)
        {
            fatal("Unable to prompt for input: " + e.getMessage());
            return null;
        }

    }

    public Response ynaPrompt(String question, Response defaultResponse)
    {
        String choices = "Yes/No/Always";

        switch (defaultResponse)
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
            while (response == null)
            {
                System.out.print(choices + "> ");
                String input = inputReader.readLine();
                response = Response.fromInput(input, defaultResponse, Response.YES, Response.NO, Response.ALWAYS);
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
