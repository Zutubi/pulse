package com.zutubi.pulse.master.notifications.jabber.config;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

@Wire
@SymbolicName("zutubi.jabberConfigurationCheckHandler")
public class JabberConfigurationCheckHandler extends AbstractConfigurationCheckHandler<JabberConfiguration>
{
    private JabberManager jabberManager;

    private String account;

    public void setAccount(String account)
    {
        this.account = account;
    }

    public String getAccount()
    {
        return account;
    }

    public void test(JabberConfiguration configuration) throws Exception
    {
        if (!configuration.isEnabled())
        {
            throw new PulseException("Jabber configuration is disabled.");
        }
        jabberManager.testConnection(configuration, account);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
