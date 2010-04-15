package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.RecordManager;

/**
 * Used for health problems that have no automatic solution.
 */
public class UnsolvableHealthProblem extends HealthProblemSupport implements HealthProblem
{
    protected UnsolvableHealthProblem(String path, String message)
    {
        super(path, message);
    }

    public boolean isSolvable()
    {
        return false;
    }

    public void solve(RecordManager recordManager)
    {
        // Not solvable!
    }
}
