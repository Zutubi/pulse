package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import static org.mockito.Mockito.*;

import java.util.Arrays;

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
        assertFalse(requirements.isFulfilledBy(createRequest(new ResourceRequirement("doesnt exist", false)), mockAgentService));
    }

    public void testExistingResource()
    {
        assertTrue(requirements.isFulfilledBy(createRequest(new ResourceRequirement(RESOURCE_NAME, false)), mockAgentService));
    }

    public void testUnfulfillableReason()
    {
        assertEquals(
                "Missing one or more of the following resource. ant:[default]",
                requirements.getUnfulFilledReason(createRequest(new ResourceRequirement("ant", false)))
        );
        assertEquals(
                "Missing one or more of the following resource. ant:1.0",
                requirements.getUnfulFilledReason(createRequest(new ResourceRequirement("ant", "1.0", false)))
        );
        assertEquals(
                "Missing one or more of the following resource. ant:1.0, make:2.0",
                requirements.getUnfulFilledReason(createRequest(new ResourceRequirement("ant", "1.0", false), new ResourceRequirement("make", "2.0", false)))
        );
    }

    private RecipeAssignmentRequest createRequest(ResourceRequirement... resourceRequirements)
    {
        return new RecipeAssignmentRequest(null, null, Arrays.asList(resourceRequirements), null, null, null);
    }
}
