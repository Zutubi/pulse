package com.zutubi.pulse.acceptance.components.pulse.agent;

import com.zutubi.pulse.acceptance.SeleniumBrowser;
import com.zutubi.pulse.acceptance.components.table.SummaryTable;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.*;

/**
 * Corresponds to the Zutubi.pulse.agent.AgentSummaryTable component.
 */
public class AgentSummaryTable extends SummaryTable
{
    private static final String EXPRESSION_MENU = "var menu = " + SeleniumBrowser.CURRENT_WINDOW + ".Ext.get('menus-window');";
    private static final Set<String> TABS = new HashSet<String>(Arrays.asList("status", "statistics", "info", "messages"));
    
    public AgentSummaryTable(SeleniumBrowser browser, String id)
    {
        super(browser, id);
    }

    /**
     * Returns information about the agents in the table.
     * 
     * @return information about the agents in this table
     */
    public List<AgentInfo> getAgents()
    {
        List<AgentInfo> result = new LinkedList<AgentInfo>();
        int count = getRowCount();
        for (int i = 0; i < count; i++)
        {
            result.add(new AgentInfo(getRow(i)));
        }

        return result;
    }
    
    /**
     * Returns information about a specific agent in the table.
     * 
     * @param name name of the agent to return the info for
     * @return information about the agent, or null if it is not found
     */
    public AgentInfo getAgent(final String name)
    {
        return CollectionUtils.find(getAgents(), new Predicate<AgentInfo>()
        {
            public boolean satisfied(AgentInfo agentInfo)
            {
                return name.equals(agentInfo.name);
            }
        });
    }
    
    public String getStatus(String agent)
    {
        final AgentInfo info = getAgent(agent);
        return info == null ? null : info.status;
    }
    
    public void clickAgentActionsAndWait(long agentId)
    {
        browser.evalExpression(SeleniumBrowser.CURRENT_WINDOW + ".Zutubi.FloatManager.hideAll()");
        browser.click("aactions-" + agentId + "-button");
        browser.waitForCondition(EXPRESSION_MENU + " menu && menu.isVisible()");
    }

    public void clickAction(long agentId, String action)
    {
        clickAgentActionsAndWait(agentId);
        browser.click(action + "-aactions-" + agentId);
    }
    
    public List<String> getActions(long agentId)
    {
        clickAgentActionsAndWait(agentId);
        String idString = browser.evalExpression(EXPRESSION_MENU + " var ids = []; menu.select('a').each(function(item) { ids.push(item.id); }); ids.join(',')");
        String[] ids = idString.split(",");
        List<String> result = new LinkedList<String>();
        for (String id: ids)
        {
            int index = id.indexOf('-');
            id = id.substring(0, index);
            if (!TABS.contains(id))
            {
                result.add(id);
            }
        }
        
        Collections.sort(result);
        return result;
    }
    
    public boolean areActionsAvailable(long agentId, String... actions)
    {
        final List<String> available = getActions(agentId);
        for (String action: actions)
        {
            if (!available.contains(action))
            {
                return false;
            }
        }
        
        return true;
    }
}
