package com.cinnamonbob.web;

import com.cinnamonbob.logging.CustomLogRecord;
import com.cinnamonbob.logging.ServerMessagesHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Looks up recent server log messages for the server->messages page.
 */
public class ServerMessagesAction extends ActionSupport
{
    private ServerMessagesHandler serverMessagesHandler;
    private List<CustomLogRecord> records;
    private PagingSupport pagingSupport = new PagingSupport(10);

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public List<CustomLogRecord> getRecords()
    {
        return records;
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public boolean isError(CustomLogRecord record)
    {
        return record.getLevel() == Level.SEVERE;
    }

    public boolean isWarning(CustomLogRecord record)
    {
        return record.getLevel() == Level.WARNING;
    }

    public boolean hasThrowable(CustomLogRecord record)
    {
        return record.getThrown() != null;
    }

    public String getStackTrace(CustomLogRecord record)
    {
        Throwable t = record.getThrown();
        if(t != null)
        {
            StringWriter writer = new StringWriter();
            t.printStackTrace(new PrintWriter(writer));
            return writer.getBuffer().toString();
        }
        else
        {
            return "";
        }
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
