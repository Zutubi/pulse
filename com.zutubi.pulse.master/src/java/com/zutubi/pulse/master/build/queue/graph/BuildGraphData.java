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

package com.zutubi.pulse.master.build.queue.graph;

import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * The data contained by the dependency graph node.
 */
public class BuildGraphData
{
    /**
     * Configuration of the project represented by the node itself.
     */
    private ProjectConfiguration projectConfig;

    /**
     * The dependency that was traversed to get to this node.
     * In the upstream case, the dependency references this nodes
     * project.  In the downstream case, the dependency references
     * the project that lead to this node.
     */
    private DependencyConfiguration dependency;

    public BuildGraphData(ProjectConfiguration projectConfig)
    {
        this.projectConfig = projectConfig;
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectConfig;
    }

    public DependencyConfiguration getDependency()
    {
        return dependency;
    }

    public void setDependency(DependencyConfiguration dependency)
    {
        this.dependency = dependency;
    }
}
