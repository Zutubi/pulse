package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class PersonalBuildAgentRequirementsTest extends PulseTestCase
{
    public void testBaseRequirements()
    {
        AgentRequirements buildAgentRequirements = new AgentRequirements()
        {
            public String getSummary()
            {
                return "test";
            }

            public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
            {
                return false;
            }
        };
        verifyPersonalBuildsHelper(buildAgentRequirements, true, true, false);
    }

    public void testVerifyPersonalBuilds()
    {
        AgentRequirements buildAgentRequirements = new AgentRequirements()
        {
            public String getSummary()
            {
                return "test";
            }

            public boolean fulfilledBy(RecipeAssignmentRequest request, AgentService service)
            {
                return true;
            }
        };

        verifyPersonalBuildsHelper(buildAgentRequirements, false, true, true);
        verifyPersonalBuildsHelper(buildAgentRequirements, false, false, true);
        verifyPersonalBuildsHelper(buildAgentRequirements, true, true, true);
        verifyPersonalBuildsHelper(buildAgentRequirements, true, false, false);
    }

    public void verifyPersonalBuildsHelper(AgentRequirements agentRequirements, boolean isPersonal, boolean allowPersonal, boolean expectedResult)
    {
        PersonalBuildAgentRequirements personalBuildAgentRequirements = new PersonalBuildAgentRequirements(agentRequirements);
        RecipeAssignmentRequest recipeAssignmentRequest = mock(RecipeAssignmentRequest.class);
        stub(recipeAssignmentRequest.isPersonal()).toReturn(isPersonal);
        AgentService agentService = mock(AgentService.class);
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        agentConfiguration.setAllowPersonalBuilds(allowPersonal);
        stub(agentService.getAgentConfig()).toReturn(agentConfiguration);

        assertEquals(expectedResult, personalBuildAgentRequirements.fulfilledBy(recipeAssignmentRequest, agentService));
    }

}
