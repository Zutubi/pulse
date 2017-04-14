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

package com.zutubi.pulse.master.dependency;

import com.zutubi.pulse.master.model.Project;

/**
 * Data holding class for the information in tree nodes in a project dependency
 * graph.
 */
public class DependencyGraphData
{
    private Project project;
    private boolean subtreeFiltered = false;

    public DependencyGraphData(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isSubtreeFiltered()
    {
        return subtreeFiltered;
    }

    public void markSubtreeFiltered()
    {
        subtreeFiltered = true;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        DependencyGraphData that = (DependencyGraphData) o;
        if (subtreeFiltered != that.subtreeFiltered)
        {
            return false;
        }
        if (!project.equals(that.project))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = project.hashCode();
        result = 31 * result + (subtreeFiltered ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + project.getName() + ", "  + subtreeFiltered + ")";
    }
}
