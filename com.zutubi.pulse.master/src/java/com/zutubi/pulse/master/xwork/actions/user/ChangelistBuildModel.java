package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.StringUtils;

/**
 * JSON-encodable object representing a build affected by a changelist.
 */
public class ChangelistBuildModel
{
    private BuildResult buildResult;

    public ChangelistBuildModel(BuildResult buildResult)
    {
        this.buildResult = buildResult;
    }

    public String getProject()
    {
        return buildResult.getProject().getName();
    }

    public String getEncodedProject()
    {
        return StringUtils.uriComponentEncode(getProject());
    }

    public long getNumber()
    {
        return buildResult.getNumber();
    }

    public String getStatus()
    {
        return buildResult.getState().getPrettyString();
    }

    public String getStatusIcon()
    {
        return ToveUtils.getStatusIcon(buildResult);
    }

    public String getStatusClass()
    {
        return ToveUtils.getStatusClass(buildResult);
    }
}
