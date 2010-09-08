package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;

@SymbolicName("zutubi.jabberContactConfigurationCheckHandler")
@Wire
public class JabberContactConfigurationCheckHandler extends AbstractConfigurationCheckHandler<JabberContactConfiguration>
{
    public void test(JabberContactConfiguration configuration) throws Exception
    {
        configuration.notify(null, null, "Test message from Pulse", null);
    }
}
