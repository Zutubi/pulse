package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * JSON-encodeable information about a project a user is responsible for.
 */
public class UserResponsibilityModel
{
    private String project;
    private long projectId;

    public UserResponsibilityModel(String project, long projectId)
    {
        this.project = project;
        this.projectId = projectId;
    }

    public String getId()
    {
        return getResponsibilityId(project);
    }

    public static String getResponsibilityId(String project)
    {
        return WebUtils.toValidHtmlName("responsibility-" + project);
    }

    public String getProject()
    {
        return project;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public String getUrl()
    {
        return Urls.getBaselessInstance().project(WebUtils.uriComponentEncode(project));
    }
}
