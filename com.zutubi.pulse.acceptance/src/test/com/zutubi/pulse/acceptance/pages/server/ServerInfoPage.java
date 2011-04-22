package com.zutubi.pulse.acceptance.pages.server;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.KeyValueTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The server info page shows system information for the server and VM.
 */
public class ServerInfoPage extends SeleniumPage
{
    private KeyValueTable pulseProperties;
    private KeyValueTable serverProperties;
    private KeyValueTable jvmProperties;
    private KeyValueTable environment;
    
    public ServerInfoPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, "server-info", "server info");
        
        pulseProperties = new KeyValueTable(browser, this.getId() + "-pulseProperties");
        serverProperties = new KeyValueTable(browser, this.getId() + "-serverProperties");
        jvmProperties = new KeyValueTable(browser, this.getId() + "-jvmProperties");
        environment = new KeyValueTable(browser, this.getId() + "-environment");
    }

    public String getUrl()
    {
        return urls.serverInfo();
    }

    public KeyValueTable getPulseProperties()
    {
        return pulseProperties;
    }

    public KeyValueTable getServerProperties()
    {
        return serverProperties;
    }

    public KeyValueTable getJvmProperties()
    {
        return jvmProperties;
    }

    public KeyValueTable getEnvironment()
    {
        return environment;
    }
}
