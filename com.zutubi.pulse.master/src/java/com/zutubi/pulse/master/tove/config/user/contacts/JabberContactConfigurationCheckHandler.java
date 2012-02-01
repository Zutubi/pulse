package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

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
        configuration.notify(new RenderedResult(null, "Test message from Pulse", null), null);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
