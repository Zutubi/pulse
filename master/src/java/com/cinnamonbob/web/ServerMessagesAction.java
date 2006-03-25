package com.cinnamonbob.web;

import com.cinnamonbob.logging.ServerMessagesHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Looks up recent server log messages for the server->messages page.
 */
public class ServerMessagesAction extends ActionSupport
{
    private ServerMessagesHandler serverMessagesHandler;
    private List<LogRecord> records;
    private PagingSupport pagingSupport = new PagingSupport(10);

    public void setStartPage(int page)
    {
        pagingSupport.setStartPage(page);
    }

    public List<LogRecord> getRecords()
    {
        return records;
    }

    public PagingSupport getPagingSupport()
    {
        return pagingSupport;
    }

    public boolean isError(LogRecord record)
    {
        return record.getLevel() == Level.SEVERE;
    }

    public boolean isWarning(LogRecord record)
    {
        return record.getLevel() == Level.WARNING;
    }

    public boolean hasThrowable(LogRecord record)
    {
        return record.getThrown() != null;
    }
    
    public String getStackTrace(LogRecord record)
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
