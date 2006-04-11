package com.zutubi.pulse.scm.cvs.client;

import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.core.model.Change;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The HistoryInfo object represents a single line from the history output.
 */
public class HistoryInfo
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

    static final SimpleDateFormat LOGDATE = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");


    public HistoryInfo(String data) throws SCMException
    {
        StringTokenizer tokenizer = new StringTokenizer(data, " ", false);
        List<String> tokens = new ArrayList<String>();
        while (tokenizer.hasMoreTokens())
        {
            tokens.add(tokenizer.nextToken());
        }

        if (tokens.size() < 8)
        {
            throw new SCMException("Unable to extract history info from data: " + data);
        }

        this.code = (tokens.get(0));
        this.date = (tokens.get(1)); // date
        this.time = (tokens.get(2)); // time
        this.timezone = (tokens.get(3)); // timezone
        this.user = (tokens.get(4));

        if (isUpdate() || isCommit())
        {
            this.revision = (tokens.get(5)); // file version
            this.file = (tokens.get(6)); // file name
            this.pathInRepository = (tokens.get(7)); // path in repository
            tokens.get(8); // ==
            this.workingpath = (tokens.get(9)); // working path
        }
        else
        {
            this.file = (tokens.get(5)); // file
            this.pathInRepository = (tokens.get(6).substring(1, tokens.get(6).length() - 1)); // =path in repository=
            this.workingpath = (tokens.get(7)); // working path
        }
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
                return LOGDATE.parse(getDate() + " " + getTime() + " " + getTimezone());
            }
            return null;
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
