package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import static org.mockito.Mockito.*;

import java.util.Arrays;

public class AnyCapableAgentRequirementsTest extends PulseTestCase
{
    private static final String EXISTING_RESOURCE = "existingresource";
    private static final String EXISTING_VERSION = "existingversion";

    private AnyCapableAgentRequirements requirements = new AnyCapableAgentRequirements();
    private AgentService mockAgentService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockAgentService = mock(AgentService.class);
        stub(mockAgentService.hasResource((ResourceRequirement) anyObject())).toReturn(false);
        stub(mockAgentService.hasResource(new ResourceRequirement(EXISTING_RESOURCE, false))).toReturn(true);
        stub(mockAgentService.hasResource(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, false))).toReturn(true);
    }

    public void testNoResources()
    {
        assertTrue(requirements.fulfilledBy(createRequest(), mockAgentService));
    }

    public void testNonExistantResource()
    {
        assertFalse(requirements.fulfilledBy(createRequest(new ResourceRequirement("doesnt exist", false)), mockAgentService));
    }

    public void testNonExistantResourceOptional()
    {
        assertTrue(requirements.fulfilledBy(createRequest(new ResourceRequirement("doesnt exist", true)), mockAgentService));
    }

    public void testExistingResourceDefaultVersion()
    {
        assertTrue(requirements.fulfilledBy(createRequest(new ResourceRequirement(EXISTING_RESOURCE, false)), mockAgentService));
    }

    public void testExistingResourceExistingVersion()
    {
        assertTrue(requirements.fulfilledBy(createRequest(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, false)), mockAgentService));
    }

    public void testExistingResourceNonExistantVersion()
    {
        assertFalse(requirements.fulfilledBy(createRequest(new ResourceRequirement(EXISTING_RESOURCE, "nope", false)), mockAgentService));
    }

    public void testExistingResourceNonExistantVersionOptional()
    {
        assertTrue(requirements.fulfilledBy(createRequest(new ResourceRequirement(EXISTING_RESOURCE, "nope", true)), mockAgentService));
    }

    private RecipeAssignmentRequest createRequest(ResourceRequirement... resourceRequirements)
    {
        return new RecipeAssignmentRequest(null, null, Arrays.asList(resourceRequirements), null, null, null);
    }
}
