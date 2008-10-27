package com.zutubi.pulse.master.jabber.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.tove.config.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.master.jabber.JabberManager;
import org.jivesoftware.smack.XMPPException;

/**
 */
@Wire
@SymbolicName("zutubi.jabberConfigurationCheckHandler")
public class JabberConfigurationCheckHandler extends ConfigurationCheckHandlerSupport<JabberConfiguration>
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
