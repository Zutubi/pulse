package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.logging.ServerMessagesHandler;
import com.zutubi.pulse.web.agents.ServerMessagesActionSupport;
import com.zutubi.pulse.web.PagingSupport;
import com.caucho.hessian.client.HessianRuntimeException;

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
        lookupSlave();

        try
        {
            records = getAgent().getRecentMessages();
            Collections.reverse(records);
            pagingSupport.setTotalItems(records.size());
            records = records.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());
        }
        catch(HessianRuntimeException e)
        {
            addActionError("Unable to contact agent: " + e.getMessage());
        }

        return SUCCESS;
    }
}
