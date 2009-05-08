package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;

/**
 * JSON-encodeable information about a build a user is responsible for.
 */
public class ResponsibilityModel
{
    private String project;
    private long buildId;
    private long number;

    public ResponsibilityModel(String project, long buildId, long number)
    {
        this.project = project;
        this.buildId = buildId;
        this.number = number;
    }

    public String getId()
    {
        return getResponsibilityId(project, number);
    }

    public static String getResponsibilityId(String project, long number)
    {
        return StringUtils.toValidHtmlName("responsibility-" + project + "-" + number);
    }

    public String getProject()
    {
        return project;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public long getNumber()
    {
        return number;
    }

    public String getUrl()
    {
        return Urls.getBaselessInstance().project(StringUtils.uriComponentEncode(project));
    }
}
