package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;

@Wire
@SymbolicName("zutubi.jabberContactConfigurationCheckHandler")
public class JabberContactConfigurationCheckHandler extends AbstractConfigurationCheckHandler<JabberContactConfiguration>
{
    private JabberManager jabberManager;

    public void test(JabberContactConfiguration configuration) throws Exception
    {
        if (!jabberManager.isConfigured())
        {
            throw new PulseException("Jabber is disabled.");
        }
        configuration.notify(null, null, "Test message from Pulse", null);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
