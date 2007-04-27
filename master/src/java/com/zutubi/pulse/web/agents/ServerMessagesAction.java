package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.web.PagingSupport;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent server log messages for the server->messages page.
 */
public class ServerMessagesAction extends ServerMessagesActionSupport
{
    private List<CustomLogRecord> records;
    private PagingSupport pagingSupport = new PagingSupport(10);

    public List<CustomLogRecord> getRecords()
    {
        return records;
    }

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public String execute() throws Exception
    {
        Agent agent = getAgent();
        if(agent.isOnline())
        {
            try
            {
                records = agent.getService().getRecentMessages();
                Collections.reverse(records);
                pagingSupport.setTotalItems(records.size());
                records = records.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
            }
            catch(RuntimeException e)
            {
                addActionError("Unable to contact agent: " + e.getMessage());
            }
        }
        else
        {
            addActionError("Agent is not online.");
        }
        
        return SUCCESS;
    }
}
