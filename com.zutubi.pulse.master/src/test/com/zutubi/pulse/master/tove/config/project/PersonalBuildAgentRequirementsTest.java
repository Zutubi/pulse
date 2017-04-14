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
        AgentRequirements buildAgentRequirements = new TestAgentRequirements(false);
        verifyPersonalBuildsHelper(buildAgentRequirements, true, true, false);
    }

    public void testUnfulfilledReason()
    {
        AgentRequirements agentRequirements = new TestAgentRequirements(false);

        PersonalBuildAgentRequirements personalBuildAgentRequirements = new PersonalBuildAgentRequirements(agentRequirements);
        RecipeAssignmentRequest request = mock(RecipeAssignmentRequest.class);
        stub(request.isPersonal()).toReturn(true);

        assertEquals("Only considering agents that allow personal builds. test", personalBuildAgentRequirements.getUnfulfilledReason(request));
    }

    public void testVerifyPersonalBuilds()
    {
        AgentRequirements buildAgentRequirements = new TestAgentRequirements(true);

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

        assertEquals(expectedResult, personalBuildAgentRequirements.isFulfilledBy(recipeAssignmentRequest, agentService));
    }

    private class TestAgentRequirements implements AgentRequirements
    {
        private boolean fulfilled = false;

        private TestAgentRequirements(boolean fulfilled)
        {
            this.fulfilled = fulfilled;
        }

        public String getSummary()
        {
            return "test";
        }

        public String getUnfulfilledReason(RecipeAssignmentRequest request)
        {
            return "test";
        }

        public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
        {
            return fulfilled;
        }
    }
}
