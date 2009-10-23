package com.zutubi.pulse.core.personal;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;

import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of {@link com.zutubi.pulse.core.scm.api.PersonalBuildUI}
 * that just captures messages and gives blank/default responses for testing
 * purposes.
 */
public class TestPersonalBuildUI implements PersonalBuildUI
{
    private List<String> debugMessages = new LinkedList<String>();
    private List<String> statusMessages = new LinkedList<String>();
    private List<String> warningMessages = new LinkedList<String>();
    private List<String> errorMessages = new LinkedList<String>();

    public List<String> getDebugMessages()
    {
        return debugMessages;
    }

    public List<String> getStatusMessages()
    {
        return statusMessages;
    }

    public List<String> getWarningMessages()
    {
        return warningMessages;
    }

    public List<String> getErrorMessages()
    {
        return errorMessages;
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public void debug(String message)
    {
        debugMessages.add(message);
    }

    public void status(String message)
    {
        statusMessages.add(message);
    }

    public void warning(String message)
    {
        warningMessages.add(message);
    }

    public void error(String message)
    {
        errorMessages.add(message);
    }

    public void error(String message, Throwable throwable)
    {
        errorMessages.add(message + ": " + throwable.getMessage());
    }

    public void enterContext()
    {
    }

    public void exitContext()
    {
    }

    public String inputPrompt(String question)
    {
        return "test input";
    }

    public String inputPrompt(String prompt, String defaultResponse)
    {
        return defaultResponse;
    }

    public String passwordPrompt(String question)
    {
        return "test password";
    }

    public Response ynPrompt(String question, Response defaultResponse)
    {
        return defaultResponse;
    }

    public Response ynaPrompt(String question, Response defaultResponse)
    {
        return defaultResponse;
    }
}
