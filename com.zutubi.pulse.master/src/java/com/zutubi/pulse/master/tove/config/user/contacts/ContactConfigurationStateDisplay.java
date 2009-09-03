package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.notifications.ResultNotifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Shows a contact point error if one exists.
 */
public class ContactConfigurationStateDisplay
{
    private ResultNotifier resultNotifier;

    public List<String> getFields(ContactConfiguration contactConfiguration)
    {
        if (resultNotifier.hasError(contactConfiguration))
        {
            return Arrays.asList("lastError");
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public String formatLastError(ContactConfiguration contactConfiguration)
    {
        return resultNotifier.getError(contactConfiguration);
    }

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }
}
