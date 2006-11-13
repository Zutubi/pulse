package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Changelist;

import java.util.List;
import java.util.LinkedList;

/**
 */
public class ChangelistUtils
{
    public static List<BuildResult> getBuilds(BuildManager buildManager, Changelist list)
    {
        List<BuildResult> result = new LinkedList<BuildResult>();
        for(long buildId: list.getResultIds())
        {
            result.add(buildManager.getBuildResult(buildId));
        }

        return result;
    }
    
}
