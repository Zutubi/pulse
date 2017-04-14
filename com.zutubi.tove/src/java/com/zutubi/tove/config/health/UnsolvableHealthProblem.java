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
