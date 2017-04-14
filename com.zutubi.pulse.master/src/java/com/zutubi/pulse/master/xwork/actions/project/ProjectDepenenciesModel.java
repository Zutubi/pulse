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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.util.adt.Grid;

/**
 * JSON structure for the project dependencies page.
 */
public class ProjectDepenenciesModel
{
    private Grid<ProjectDependencyData> upstream;
    private Grid<ProjectDependencyData> downstream;

    public ProjectDepenenciesModel(Grid<ProjectDependencyData> upstream, Grid<ProjectDependencyData> downstream)
    {
        this.upstream = upstream;
        this.downstream = downstream;
    }

    public Grid<ProjectDependencyData> getUpstream()
    {
        return upstream;
    }

    public Grid<ProjectDependencyData> getDownstream()
    {
        return downstream;
    }
}
