package com.zutubi.pulse.web.agents;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.web.PagingSupport;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent server log messages for the server->messages page.
 */
public class ServerMessagesAction extends ServerMessagesActionSupport
{
    private static final Logger LOG = Logger.getLogger(ServerMessagesAction.class);
    
    private List<CustomLogRecord> records = null;
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
        if(agent == null)
        {
            records = serverMessagesHandler.takeSnapshot();
        }
        else
        {
            if(agent.isOnline())
            {
                try
                {
                    records = agent.getService().getRecentMessages();
                }
                catch(HessianRuntimeException e)
                {
                    LOG.warning(e);
                    addActionError("Unable to contact agent: " + e.getMessage());
                }
            }
            else
            {
                addActionError("Agent is not online.");
            }
        }

        if(records != null)
        {
            Collections.reverse(records);
            pagingSupport.setTotalItems(records.size());
            records = records.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
        }

        return SUCCESS;
    }
}
