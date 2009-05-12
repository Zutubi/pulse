package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * JSON-encodeable information about a project a user is responsible for.
 */
public class ResponsibilityModel
{
    private String project;
    private long projectId;

    public ResponsibilityModel(String project, long projectId)
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
        return StringUtils.toValidHtmlName("responsibility-" + project);
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
        return Urls.getBaselessInstance().project(StringUtils.uriComponentEncode(project));
    }
}
