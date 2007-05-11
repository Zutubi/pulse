package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.prototype.ConfigurationCheckHandlerSupport;
import com.zutubi.pulse.jabber.JabberManager;
import org.jivesoftware.smack.XMPPException;

/**
 *
 *
 */
@SymbolicName("internal.jabberConfigurationCheckHandler")
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
