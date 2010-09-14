package com.zutubi.pulse.core.ui;

import com.zutubi.pulse.core.ui.api.MenuChoice;
import com.zutubi.pulse.core.ui.api.MenuOption;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of {@link com.zutubi.pulse.core.ui.api.UserInterface}
 * that just captures messages and gives blank/default responses for testing
 * purposes.
 */
public class TestUI implements UserInterface
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

    public YesNoResponse yesNoPrompt(String question, boolean showAlways, boolean showNever, YesNoResponse defaultResponse)
    {
        return defaultResponse;
    }

    public <T> MenuChoice<T> menuPrompt(String question, List<MenuOption<T>> choices)
    {
        MenuOption<T> defaultOption = CollectionUtils.find(choices, new Predicate<MenuOption<T>>()
        {
            public boolean satisfied(MenuOption<T> option)
            {
                return option.isDefaultOption();
            }
        });

        if (defaultOption != null)
        {
            return new MenuChoice<T>(defaultOption.getValue(), false);
        }
        else
        {
            throw new RuntimeException("No default option");
        }
    }
}
