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

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.api.Configuration;
import static java.util.Arrays.asList;
import org.mockito.Matchers;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceRequirementsConfigurationStateDisplayTest extends PulseTestCase
{
    private static final int AGENT_COUNT = 3;

    private static final AgentConfiguration AGENT_1 = new AgentConfiguration("one");
    private static final AgentConfiguration AGENT_2 = new AgentConfiguration("two");
    private static final AgentConfiguration AGENT_3 = new AgentConfiguration("three");

    private static final ResourceRequirement REQUIREMENT_1 = new ResourceRequirement("r1", false, false);
    private static final ResourceRequirement REQUIREMENT_1_AND_2 = new ResourceRequirement("rboth", false, false);
    private static final ResourceRequirement REQUIREMENT_NONE = new ResourceRequirement("rnone", false, false);

    private static final ResourceRequirementConfiguration REQUIREMENT_CONFIG_1 = new ResourceRequirementConfiguration(REQUIREMENT_1);
    private static final ResourceRequirementConfiguration REQUIREMENT_CONFIG_1_AND_2 = new ResourceRequirementConfiguration(REQUIREMENT_1_AND_2);
    private static final ResourceRequirementConfiguration REQUIREMENT_CONFIG_NONE = new ResourceRequirementConfiguration(REQUIREMENT_NONE);

    private static final List<ResourceRequirement> EMPTY_REQUIREMENTS = Collections.emptyList();
    private static final List<ResourceRequirement> ONLY_1_REQUIREMENTS = asList(REQUIREMENT_1);
    private static final List<ResourceRequirement> BOTH_1_AND_2_REQUIREMENTS = asList(REQUIREMENT_1_AND_2);
    private static final List<ResourceRequirement> NONE_REQUIREMENTS = asList(REQUIREMENT_NONE);

    private static final List<ResourceRequirementConfiguration> EMPTY_REQUIREMENT_CONFIGS = Collections.emptyList();
    private static final List<ResourceRequirementConfiguration> ONLY_1_REQUIREMENT_CONFIGS = asList(REQUIREMENT_CONFIG_1);
    private static final List<ResourceRequirementConfiguration> BOTH_1_AND_2_REQUIREMENT_CONFIGS = asList(REQUIREMENT_CONFIG_1_AND_2);
    private static final List<ResourceRequirementConfiguration> NONE_REQUIREMENT_CONFIGS = asList(REQUIREMENT_CONFIG_NONE);

    private ResourceRequirementConfigurationStateDisplay display;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        display = new ResourceRequirementConfigurationStateDisplay();

        AgentManager agentManager = mock(AgentManager.class);
        doReturn(AGENT_COUNT).when(agentManager).getAgentCount();
        display.setAgentManager(agentManager);

        ProjectConfiguration owningProject = new ProjectConfiguration();
        owningProject.setRequirements(ONLY_1_REQUIREMENT_CONFIGS);
        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        doReturn(owningProject).when(configurationProvider).getAncestorOfType(Matchers.<Configuration>anyObject(), eq(ProjectConfiguration.class));
        display.setConfigurationProvider(configurationProvider);

        ResourceManager resourceManager = mock(ResourceManager.class);
        doReturn(asSet(AGENT_1, AGENT_2, AGENT_3)).when(resourceManager).getCapableAgents(EMPTY_REQUIREMENTS);
        doReturn(asSet(AGENT_1, AGENT_2)).when(resourceManager).getCapableAgents(BOTH_1_AND_2_REQUIREMENTS);
        doReturn(asSet(AGENT_1)).when(resourceManager).getCapableAgents(ONLY_1_REQUIREMENTS);
        doReturn(asSet()).when(resourceManager).getCapableAgents(NONE_REQUIREMENTS);
        display.setResourceManager(resourceManager);

        display.setMessages(Messages.getInstance(ResourceRequirementConfiguration.class));
    }

    public void testNoAgents()
    {
        assertEquals("none", display.formatCollectionCompatibleAgents(NONE_REQUIREMENT_CONFIGS, new ProjectConfiguration()));
    }

    public void testOneAgent()
    {
        assertEquals(AGENT_1.getName(), display.formatCollectionCompatibleAgents(ONLY_1_REQUIREMENT_CONFIGS, new ProjectConfiguration()));
    }

    public void testSomeAgents()
    {
        assertEquals(asList(AGENT_1.getName(), AGENT_2.getName()), display.formatCollectionCompatibleAgents(BOTH_1_AND_2_REQUIREMENT_CONFIGS, new ProjectConfiguration()));
    }

    public void testAllAgents()
    {
        assertEquals("all agents", display.formatCollectionCompatibleAgents(EMPTY_REQUIREMENT_CONFIGS, new ProjectConfiguration()));
    }

    public void testConsidersProjectRequirementsForStage()
    {
        assertEquals(AGENT_1.getName(), display.formatCollectionCompatibleAgents(EMPTY_REQUIREMENT_CONFIGS, new BuildStageConfiguration()));
    }

    private Set<AgentConfiguration> asSet(AgentConfiguration... agents)
    {
        return new HashSet<AgentConfiguration>(asList(agents));
    }
}
