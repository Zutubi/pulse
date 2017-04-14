/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
