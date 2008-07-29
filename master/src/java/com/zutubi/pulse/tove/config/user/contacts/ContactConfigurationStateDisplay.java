package com.zutubi.pulse.tove.config.user.contacts;

import com.zutubi.pulse.ResultNotifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Shows a contact point error if one exists.
 */
public class ContactConfigurationStateDisplay
{
    private ResultNotifier resultNotifier;

    @SuppressWarnings({"unchecked"})
    public List<String> getFields(ContactConfiguration contactConfiguration)
    {
        if (resultNotifier.hasError(contactConfiguration))
        {
            return Arrays.asList("lastError");
        }
        else
        {
            return Collections.EMPTY_LIST;
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
