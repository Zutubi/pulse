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

package com.zutubi.pulse.master.upgrade;

import java.util.LinkedList;
import java.util.List;

/**
 * A holder for a group of upgrade tasks some a single source upgradeable component
 */
public class UpgradeTaskGroup
{
    private UpgradeableComponent source;

    private List<UpgradeTask> tasks = new LinkedList<UpgradeTask>();

    public UpgradeableComponent getSource()
    {
        return source;
    }

    public void setSource(UpgradeableComponent source)
    {
        this.source = source;
    }

    public List<UpgradeTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(List<UpgradeTask> tasks)
    {
        this.tasks = tasks;
    }
}
