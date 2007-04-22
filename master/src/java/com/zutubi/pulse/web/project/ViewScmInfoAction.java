package com.zutubi.pulse.web.project;

import com.zutubi.pulse.servercore.config.ScmConfiguration;

import java.util.Map;

/**
 * Used for browsing the SCM for tyhe purpose of slecting a file/directory.
 */
public class ViewScmInfoAction extends AbstractBrowseDirAction
{
    private long id;
    private Map<String, String> info;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Map<String, String> getInfo()
    {
        return info;
    }

    public String execute()
    {
        try
        {
            ScmConfiguration scm = getProjectConfig().getScm();
            info = scm.createClient().getServerInfo();

            return SUCCESS;
        }
        catch (Exception e)
        {
            addActionError("Error retrieving SCM info: " + e.getMessage());
            return ERROR;
        }
    }
}
