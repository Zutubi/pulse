package com.zutubi.prototype.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class ResetAction extends ActionSupport
{
    private RecordManager recordManager;

    private String symbolicName;
    private String path;
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws Exception
    {
        if (!TextUtils.stringSet(path))
        {
            return INPUT;
        }

        configuration = new Configuration(path);
        configuration.analyse();

        recordManager.delete(path);

        return SUCCESS;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
