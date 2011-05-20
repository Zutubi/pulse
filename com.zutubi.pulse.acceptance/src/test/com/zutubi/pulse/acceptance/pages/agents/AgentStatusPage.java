package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.KeyValueTable;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.master.webwork.Urls;

/**
 * The status tab for a specific agent.
 */
public class AgentStatusPage extends SeleniumPage
{
    private String agent;
    private PropertyTable infoTable;
    private KeyValueTable statusTable;
    private ExecutingStageTable executingStageTable;
    private SynchronisationMessageTable synchronisationMessagesTable;
    
    public AgentStatusPage(SeleniumBrowser browser, Urls urls, String agent)
    {
        super(browser, urls, "agent-status", "agent status");
        this.agent = agent;
        infoTable = new PropertyTable(browser, getId() + "-info");
        statusTable = new KeyValueTable(browser, getId() + "-status");
        executingStageTable = new ExecutingStageTable(browser, getId() + "-executingStage");
        synchronisationMessagesTable = new SynchronisationMessageTable(browser, getId() + "-synchronisationMessages");
    }

    public String getUrl()
    {
        return urls.agentStatus(agent);
    }

    public PropertyTable getInfoTable()
    {
        return infoTable;
    }

    public KeyValueTable getStatusTable()
    {
        return statusTable;
    }

    public ExecutingStageTable getExecutingStageTable()
    {
        return executingStageTable;
    }

    public SynchronisationMessageTable getSynchronisationMessagesTable()
    {
        return synchronisationMessagesTable;
    }
}
