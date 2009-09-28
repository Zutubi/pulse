package com.zutubi.pulse.core.scm.cvs.client.commands;

import com.zutubi.pulse.core.scm.api.FileChange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The HistoryInfo object represents a single line from the history output.
 */
public class HistoryInfo
{
    protected String code;
    protected String pathInRepository;
    protected String user;
    protected String file;
    protected String revision;
    protected String date;
    protected String time;
    protected String timezone;
    protected String workingpath;

    private SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");

    public HistoryInfo() 
    {
    }

    public boolean isCheckout()
    {
//O		Checkout
        return "O".equals(getCode());
    }

    public boolean isUpdate()
    {
//W		Update (no user file, remove from entries file)
//U		Update (file overwrote unmodified user file)
//G		Update (file was merged successfully into modified user file)
//C		Update (file was merged, but conflicts w/ modified user file)
        return "W".equals(getCode()) || "U".equals(getCode()) || "G".equals(getCode()) || "C".equals(getCode());
    }

    public boolean isCommit()
    {
//M		Commit (from modified file)
//A		Commit (an added file)
//R		Commit (the removal of a file)
        return isModified() || isAdded() || isRemoved();
    }

    public boolean isModified()
    {
        return "M".equals(getCode());
    }

    public boolean isAdded()
    {
        return "A".equals(getCode());
    }

    public boolean isRemoved()
    {
        return "R".equals(getCode());
    }

    public boolean isTag()
    {
        return "T".equals(getCode());
    }

    public boolean isExport()
    {
        return "E".equals(getCode());
    }

    public boolean isRelease()
    {
        return "F".equals(getCode());
    }

    public String getUser()
    {
        return user;
    }

    public String getFile()
    {
        return file;
    }

    public String getPathInRepository()
    {
        return pathInRepository;
    }

    public String getRevision()
    {
        return revision;
    }

    public String getCode()
    {
        return code;
    }

    public String getDate()
    {
        return date;
    }

    public String getTime()
    {
        return time;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public String getWorkingpath()
    {
        return workingpath;
    }

    public Date getInfoDate()
    {
        try
        {
            if (getDate() != null)
            {
                return logDate.parse(getDate() + " " + getTime() + " " + getTimezone());
            }
            return null;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public FileChange.Action getAction()
    {
        if (isCommit())
        {
            if (isAdded())
            {
                return FileChange.Action.ADD;
            }
            else if (isRemoved())
            {
                return FileChange.Action.DELETE;
            }
            else if (isModified())
            {
                return FileChange.Action.EDIT;
            }
        }
        return FileChange.Action.UNKNOWN;
    }
}
