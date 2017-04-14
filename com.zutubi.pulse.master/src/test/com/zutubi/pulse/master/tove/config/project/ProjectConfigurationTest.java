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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.util.junit.ZutubiTestCase;

public class ProjectConfigurationTest extends ZutubiTestCase
{
    private long handles = 1;

    public void testIsDependentWithNoDependents()
    {
        ProjectConfiguration clientA = project("clientA");
        ProjectConfiguration clientB = project("clientB");
        assertFalse(clientA.isDependentOn(clientB));
    }

    public void testIsDependentWithDependencies()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration libA = project("libA");
        ProjectConfiguration libB = project("libB");
        addDependency(client, libA, libB);
        assertTrue(client.isDependentOn(libA));
        assertTrue(client.isDependentOn(libB));
        assertFalse(libA.isDependentOn(libB));
    }

    public void testIsDependentWithTransitiveDependencies()
    {
        ProjectConfiguration client = project("client");
        ProjectConfiguration lib = project("lib");
        ProjectConfiguration util = project("util");
        addDependency(client, lib);
        addDependency(lib, util);
        assertTrue(client.isDependentOn(lib));
        assertTrue(client.isDependentOn(util));
        assertTrue(lib.isDependentOn(util));
    }

    private ProjectConfiguration project(String name)
    {
        ProjectConfiguration project = new ProjectConfiguration(name);
        project.setHandle(handles++);
        return project;
    }

    private void addDependency(ProjectConfiguration project, ProjectConfiguration... others)
    {
        for (ProjectConfiguration other : others)
        {
            DependencyConfiguration dependency = new DependencyConfiguration();
            dependency.setProject(other);
            dependency.setHandle(handles++);
            project.getDependencies().getDependencies().add(dependency);
        }
    }
}
