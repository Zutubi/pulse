package com.zutubi.pulse.acceptance.pages.agents;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.pulse.agent.AgentSummaryTable;
import com.zutubi.pulse.acceptance.pages.SeleniumPage;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.Condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The front page of the "agents" section, listing all agents.
 */
public class AgentsPage extends SeleniumPage
{
    private static final String ID_AGENTS_TABLE = "agents-agents";

    private AgentSummaryTable agentSummaryTable;
    
    public AgentsPage(SeleniumBrowser browser, Urls urls)
    {
        super(browser, urls, ID_AGENTS_TABLE, "agents");
        agentSummaryTable = new AgentSummaryTable(browser, ID_AGENTS_TABLE);
    }

    public String getUrl()
    {
        return urls.agents();
    }

    @Override
    public void waitFor()
    {
        super.waitFor();
        browser.waitForVariable("panel.initialised");
    }
    
    public AgentSummaryTable getAgentSummaryTable()
    {
        return agentSummaryTable;
    }

    public void refreshUntilStatus(final String agent, final String... candidateStatuses)
    {
        final Set<String> candidateSet = new HashSet<String>(Arrays.asList(candidateStatuses));
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                openAndWaitFor();
                final String currentStatus = agentSummaryTable.getStatus(agent);
                return candidateSet.contains(currentStatus);
            }
        }, SeleniumBrowser.REFRESH_TIMEOUT, "agent status to be in " + candidateSet);        
    }
}
