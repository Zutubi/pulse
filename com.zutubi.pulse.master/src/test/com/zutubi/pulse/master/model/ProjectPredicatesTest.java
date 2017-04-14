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

package com.zutubi.pulse.master.model;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import static com.zutubi.pulse.master.model.ProjectPredicates.exists;
import static com.zutubi.pulse.master.model.ProjectPredicates.notExists;

public class ProjectPredicatesTest extends ZutubiTestCase
{
    public void testExistsForNull()
    {
        assertFalse(exists(null));
        assertTrue(notExists(null));
    }

    public void testExistsForOrphaned()
    {
        Project orphan = createOrphaned();
        assertFalse(exists(orphan));
        assertTrue(notExists(orphan));
    }

    public void testExistsForExistingProject()
    {
        Project p = new Project();
        p.setConfig(new ProjectConfiguration());
        assertTrue(exists(p));
        assertFalse(notExists(p));
    }

    public void testConcrete()
    {
        ProjectConfiguration project = createConcrete();
        assertTrue(ProjectPredicates.concrete(project));
    }

    private Project createOrphaned()
    {
        return new Project();
    }

    private ProjectConfiguration createConcrete()
    {
        ProjectConfiguration concreteProject = new ProjectConfiguration();
        concreteProject.setConcrete(true);
        return concreteProject;
    }
}
