package com.zutubi.pulse.web.user;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.User;

/**
 */
public class CustomiseBuildColumnsAction extends UserActionSupport
{
    private String suffix;
    private String columns;

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public void setColumns(String columns)
    {
        this.columns = columns;
    }

    public String execute() throws Exception
    {
        User user = getUser();
        if(user == null)
        {
            return ERROR;
        }

        if(!TextUtils.stringSet(suffix))
        {
            return ERROR;
        }

        if(suffix.equals("my"))
        {
            user.setMyBuildsColumns(columns);
        }
        else if(suffix.equals("my.projects"))
        {
            user.setMyProjectsColumns(columns);
        }
        else if(suffix.equals("all.projects"))
        {
            user.setAllProjectsColumns(columns);
        }
        else if(suffix.equals("project.summary"))
        {
            user.setProjectSummaryColumns(columns);
        }
        else if(suffix.equals("project.recent"))
        {
            user.setProjectRecentColumns(columns);
        }
        else if(suffix.equals("project.history"))
        {
            user.setProjectHistoryColumns(columns);
        }

        getUserManager().save(user);
        return SUCCESS;
    }
}
