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

package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.core.ui.TestUI;

public class AcceptancePersonalBuildUI extends TestUI
{
    private long buildNumber = -1;

    public boolean isPatchAccepted()
    {
        return buildNumber > 0;
    }

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void status(String message)
    {
        super.status(message);

        if (message.startsWith("Patch accepted"))
        {
            String[] pieces = message.split(" ");
            String number = pieces[pieces.length - 1];
            number = number.substring(0, number.length() - 1);
            buildNumber = Long.parseLong(number);
        }
    }

    public String inputPrompt(String question)
    {
        return "";
    }

    public String passwordPrompt(String question)
    {
        return "";
    }
}
