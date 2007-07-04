package com.zutubi.pulse.jabber.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.jabber.JabberManager;
import org.jivesoftware.smack.XMPPException;

/**
 *
 *
 */
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
