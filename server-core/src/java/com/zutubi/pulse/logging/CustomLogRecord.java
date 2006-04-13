/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.logging;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 */
public class CustomLogRecord
{
    private LogRecord logRecord;
    private int count = 1;

    public CustomLogRecord(LogRecord logRecord)
    {
        this.logRecord = logRecord;
    }

    public String getLoggerName()
    {
        return logRecord.getLoggerName();
    }

    public void setLoggerName(String name)
    {
        logRecord.setLoggerName(name);
    }

    public ResourceBundle getResourceBundle()
    {
        return logRecord.getResourceBundle();
    }

    public void setResourceBundle(ResourceBundle bundle)
    {
        logRecord.setResourceBundle(bundle);
    }

    public String getResourceBundleName()
    {
        return logRecord.getResourceBundleName();
    }

    public void setResourceBundleName(String name)
    {
        logRecord.setResourceBundleName(name);
    }

    public Level getLevel()
    {
        return logRecord.getLevel();
    }

    public void setLevel(Level level)
    {
        logRecord.setLevel(level);
    }

    public long getSequenceNumber()
    {
        return logRecord.getSequenceNumber();
    }

    public void setSequenceNumber(long seq)
    {
        logRecord.setSequenceNumber(seq);
    }

    public String getSourceClassName()
    {
        return logRecord.getSourceClassName();
    }

    public void setSourceClassName(String sourceClassName)
    {
        logRecord.setSourceClassName(sourceClassName);
    }

    public String getSourceMethodName()
    {
        return logRecord.getSourceMethodName();
    }

    public void setSourceMethodName(String sourceMethodName)
    {
        logRecord.setSourceMethodName(sourceMethodName);
    }

    public String getMessage()
    {
        return logRecord.getMessage();
    }

    public void setMessage(String message)
    {
        logRecord.setMessage(message);
    }

    public Object[] getParameters()
    {
        return logRecord.getParameters();
    }

    public void setParameters(Object[] parameters)
    {
        logRecord.setParameters(parameters);
    }

    public int getThreadID()
    {
        return logRecord.getThreadID();
    }

    public void setThreadID(int threadID)
    {
        logRecord.setThreadID(threadID);
    }

    public long getMillis()
    {
        return logRecord.getMillis();
    }

    public void setMillis(long millis)
    {
        logRecord.setMillis(millis);
    }

    public Throwable getThrown()
    {
        return logRecord.getThrown();
    }

    public void setThrown(Throwable thrown)
    {
        logRecord.setThrown(thrown);
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public void repeated()
    {
        count++;
    }
}
