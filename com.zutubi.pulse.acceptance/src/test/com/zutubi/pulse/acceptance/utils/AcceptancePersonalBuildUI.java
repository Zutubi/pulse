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
