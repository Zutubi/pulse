package com.zutubi.pulse.master.jabber.config;

import com.zutubi.pulse.master.jabber.JabberManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import org.jivesoftware.smack.XMPPException;

/**
 */
@Wire
@SymbolicName("zutubi.jabberConfigurationCheckHandler")
public class JabberConfigurationCheckHandler extends AbstractConfigurationCheckHandler<JabberConfiguration>
{
    private JabberManager jabberManager;

    public void test(JabberConfiguration configuration) throws XMPPException
    {
        jabberManager.testConnection(configuration);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
