package com.zutubi.pulse.master.model;

import com.zutubi.util.Mapping;

/**
 * Maps a build result to its build number.
 */
public class BuildResultToNumberMapping implements Mapping<BuildResult, Long>
{
    public Long map(BuildResult buildResult)
    {
        return buildResult.getNumber();
    }
}
