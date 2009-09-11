package com.zutubi.pulse.master.xwork.actions.agents;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.agent.HostService;
import com.zutubi.pulse.master.xwork.actions.PagingSupport;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
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

    private HostManager hostManager;

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
        if (agent == null)
        {
            records = serverMessagesHandler.takeSnapshot();
        }
        else
        {
            if (agent.isOnline())
            {
                HostService hostService = hostManager.getServiceForHost(agent.getHost());
                try
                {
                    records = hostService.getRecentMessages();
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

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }
}
