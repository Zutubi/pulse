package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.RecordManager;

/**
 * Interface for configuration health problems.  Stores information about the
 * problem, and if possible provides a solution.
 */
public interface HealthProblem
{
    /**
     * Indicates where the problem was find.
     * 
     * @return the configuration path where the problem was found
     */
    String getPath();

    /**
     * Gives a human-readable description of the problem.
     * 
     * @return a human-readable description of this problem
     */
    String getMessage();

    /**
     * Indicates if this problem can be automatically solved by calling {@link #solve(com.zutubi.tove.type.record.RecordManager)}.
     * 
     * @return true if an attempt can be made to automatically solve this
     *         problem
     */
    boolean isSolvable();

    /**
     * Attempts to automatically resolve this problem.  Implementations of this
     * method should be as paranoid and defensive as possible, as multiple
     * problems may need solving.
     * 
     * @param recordManager used to access records for solving the problem
     */
    void solve(RecordManager recordManager);
}
