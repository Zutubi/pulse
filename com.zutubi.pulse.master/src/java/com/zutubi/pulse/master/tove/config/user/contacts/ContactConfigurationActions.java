package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.notifications.ResultNotifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Action processing for {@link com.zutubi.pulse.master.tove.config.user.contacts.ContactConfiguration}'s.
 */
public class ContactConfigurationActions
{
    private ResultNotifier resultNotifier;

    public List<String> getActions(ContactConfiguration contactConfiguration)
    {
        if(resultNotifier.hasError(contactConfiguration))
        {
            return Arrays.asList("clearError");
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public void doClearError(ContactConfiguration contactConfiguration)
    {
        resultNotifier.clearError(contactConfiguration);
    }

    public void setResultNotifier(ResultNotifier resultNotifier)
    {
        this.resultNotifier = resultNotifier;
    }
}
