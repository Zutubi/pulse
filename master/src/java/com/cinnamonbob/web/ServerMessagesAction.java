package com.cinnamonbob.web;

import com.cinnamonbob.logging.CustomLogRecord;
import com.cinnamonbob.logging.ServerMessagesHandler;

import java.util.Collections;
import java.util.List;

/**
 * Looks up recent server log messages for the server->messages page.
 */
public class ServerMessagesAction extends ServerMessagesActionSupport
{
    private ServerMessagesHandler serverMessagesHandler;
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
        records = serverMessagesHandler.takeSnapshot();
        Collections.reverse(records);
        pagingSupport.setTotalItems(records.size());
        records = records.subList(pagingSupport.getStartOffset(), pagingSupport.getEndOffset());

        return SUCCESS;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }
}
