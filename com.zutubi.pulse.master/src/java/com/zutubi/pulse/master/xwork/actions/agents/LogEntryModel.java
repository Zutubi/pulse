package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.xwork.actions.project.DateModel;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.util.StringUtils;

/**
 * JSON model for a single log entry.
 */
public class LogEntryModel
{
    private DateModel when;
    private String level;
    private int count;
    private String sourceClass;
    private String sourceMethod;
    private String message;
    private String stackPreview;
    private String stackTrace;

    public LogEntryModel(CustomLogRecord record)
    {
        when = new DateModel(record.getMillis());
        level = record.getLevel().getName().toLowerCase();
        count = record.getCount();
        sourceClass = record.getSourceClassName();
        sourceMethod = record.getSourceMethodName();
        message = record.getMessage();
        stackTrace = record.getStackTrace();
        stackPreview = StringUtils.stringSet(stackTrace) ? StringUtils.getLine(stackTrace, 1) : null;
    }

    public DateModel getWhen()
    {
        return when;
    }

    public String getLevel()
    {
        return level;
    }

    public int getCount()
    {
        return count;
    }

    public String getSourceClass()
    {
        return sourceClass;
    }

    public String getSourceMethod()
    {
        return sourceMethod;
    }

    public String getMessage()
    {
        return message;
    }

    public String getStackPreview()
    {
        return stackPreview;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }
}
