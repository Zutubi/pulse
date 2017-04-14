/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.ui;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.core.ui.api.MenuChoice;
import com.zutubi.pulse.core.ui.api.MenuOption;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;

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
        MenuOption<T> defaultOption = find(choices, new Predicate<MenuOption<T>>()
        {
            public boolean apply(MenuOption<T> option)
            {
                return option.isDefaultOption();
            }
        }, null);

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
