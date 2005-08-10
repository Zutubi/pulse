package com.cinnamonbob.scm.cvs.client;

import com.cinnamonbob.scm.Change;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The HistoryInformation object represents a single line from the history output.
 */
public class HistoryInformation
{
    private String code;
    private String pathInRepository;
    private String user;
    private String file;
    private String revision;
    private String date;
    private String time;
    private String timezone;
    private String workingpath;

    static final SimpleDateFormat LOGDATE = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

    public HistoryInformation()
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

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setPathInRepository(String pathInRepository)
    {
        this.pathInRepository = pathInRepository;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public String getWorkingpath()
    {
        return workingpath;
    }

    public void setWorkingpath(String workingpath)
    {
        this.workingpath = workingpath;
    }

    public Date getInfoDate()
    {
        try
        {
            return LOGDATE.parse(date + " " + time + " " + timezone);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Change.Action getAction()
    {
        if (isCommit())
        {
            if (isAdded())
            {
                return Change.Action.ADD;
            }
            else if (isRemoved())
            {
                return Change.Action.DELETE;
            }
            else if (isModified())
            {
                return Change.Action.EDIT;
            }
        }
        return Change.Action.UNKNOWN;
    }
}
