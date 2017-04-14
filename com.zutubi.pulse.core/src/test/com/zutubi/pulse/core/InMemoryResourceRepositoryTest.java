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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Collections;

import static java.util.Arrays.asList;

public class InMemoryResourceRepositoryTest extends PulseTestCase
{
    private static final String EXISTING_RESOURCE = "existingresource";
    private static final String EXISTING_VERSION = "existingversion";

    private InMemoryResourceRepository repository;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        repository = new InMemoryResourceRepository();
        ResourceConfiguration resource = new ResourceConfiguration(EXISTING_RESOURCE);
        resource.addVersion(new ResourceVersionConfiguration(EXISTING_VERSION));
        repository.addResource(resource);
    }

    public void testSatisfiesNoResources()
    {
        assertTrue(repository.satisfies(Collections.<ResourceRequirement>emptyList()));
    }

    public void testSatisfiesNonExistantResource()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement("doesnt exist", false, false))));
    }

    public void testSatisfiesNonExistantResourceOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", false, true))));
    }

    public void testSatisfiesExistingResourceDefaultVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, false, false))));
    }

    public void testSatisfiesExistingResourceExistingVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, false, false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersion()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", false, false))));
    }

    public void testSatisfiesNonExistantResourceInverse()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", "nope", true, false))));
    }

    public void testSatisfiesNonExistantResourceInverseOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", "nope", true, true))));
    }

    public void testSatisfiesExistingResourceInverse()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, null, true, false))));
    }

    public void testSatisfiesExistingResourceExistingVersionInverse()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, true, false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersionInverse()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", true, false))));
    }

    public void testSatisfiesExistingResourceInverseOptional()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, null, true, true))));
    }

    public void testSatisfiesExistingResourceNonExistantVersionOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", false, true))));
    }
}
