package com.zutubi.pulse;

import com.zutubi.pulse.core.RecipeRequest;

/**
 * The build context contains contextual information relating to the build
 * that is currently being processed.
 *
 */
public class BuildContext
{
    private long buildNumber = -1;

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber)
    {
        this.buildNumber = buildNumber;
    }
}
