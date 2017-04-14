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

package com.zutubi.pulse.core.ui.api;

import java.util.List;

/**
 * A user interface that is non-interactive and swallows all output.
 */
public class NullUserInterface implements UserInterface
{
    public boolean isDebugEnabled()
    {
        return false;
    }

    public void debug(String message)
    {
    }

    public void status(String message)
    {
    }

    public void warning(String message)
    {
    }

    public void error(String message)
    {
    }

    public void error(String message, Throwable throwable)
    {
    }

    public void enterContext()
    {
    }

    public void exitContext()
    {
    }

    public String inputPrompt(String prompt)
    {
        return "";
    }

    public String inputPrompt(String prompt, String defaultResponse)
    {
        return defaultResponse;
    }

    public String passwordPrompt(String prompt)
    {
        return "";
    }

    public YesNoResponse yesNoPrompt(String question, boolean showAlways, boolean showNever, YesNoResponse defaultResponse)
    {
        return defaultResponse;
    }

    public <T> MenuChoice<T> menuPrompt(String question, List<MenuOption<T>> options)
    {
        return new MenuChoice<T>(options.get(0).getValue(), false);
    }
}
