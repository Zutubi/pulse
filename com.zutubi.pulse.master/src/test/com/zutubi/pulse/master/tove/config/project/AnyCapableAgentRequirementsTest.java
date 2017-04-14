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

import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class AnyCapableAgentRequirementsTest extends PulseTestCase
{
    private static final String RESOURCE_NAME = "resource";

    private AnyCapableAgentRequirements requirements = new AnyCapableAgentRequirements();
    private AgentService mockAgentService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockAgentService = mock(AgentService.class);

        InMemoryResourceRepository resourceRepository = new InMemoryResourceRepository();
        resourceRepository.addResource(new ResourceConfiguration(RESOURCE_NAME));

        ResourceManager resourceManager = mock(ResourceManager.class);
        stub(resourceManager.getAgentRepository((AgentConfiguration) anyObject())).toReturn(resourceRepository);

        requirements.setResourceManager(resourceManager);
    }

    public void testNoResources()
    {
        assertTrue(requirements.isFulfilledBy(createRequest(), mockAgentService));
    }

    public void testNonExistantResource()
    {
        assertFalse(requirements.isFulfilledBy(createRequest(new ResourceRequirement("doesnt exist", false, false)), mockAgentService));
    }

    public void testExistingResource()
    {
        assertTrue(requirements.isFulfilledBy(createRequest(new ResourceRequirement(RESOURCE_NAME, false, false)), mockAgentService));
    }

    public void testUnfulfillableReason()
    {
        assertEquals(
                "Missing one or more of the following resources. ant:[default].",
                requirements.getUnfulfilledReason(createRequest(new ResourceRequirement("ant", false, false)))
        );
        assertEquals(
                "Missing one or more of the following resources. ant:1.0.",
                requirements.getUnfulfilledReason(createRequest(new ResourceRequirement("ant", "1.0", false, false)))
        );
        assertEquals(
                "Missing one or more of the following resources. ant:1.0, make:2.0.",
                requirements.getUnfulfilledReason(createRequest(new ResourceRequirement("ant", "1.0", false, false), new ResourceRequirement("make", "2.0", false, false)))
        );
    }

    private RecipeAssignmentRequest createRequest(ResourceRequirement... resourceRequirements)
    {
        return new RecipeAssignmentRequest(null, null, Arrays.asList(resourceRequirements), null, null);
    }
}
