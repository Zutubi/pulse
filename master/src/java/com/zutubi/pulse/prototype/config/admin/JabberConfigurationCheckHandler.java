package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.ConfigurationCheckHandler;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.jabber.JabberManager;
import org.jivesoftware.smack.XMPPException;

/**
 *
 *
 */
@SymbolicName("internal.jabberConfigurationCheckHandler")
public class JabberConfigurationCheckHandler implements ConfigurationCheckHandler<JabberConfiguration>
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
