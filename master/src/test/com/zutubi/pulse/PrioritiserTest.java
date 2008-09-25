package com.zutubi.pulse;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.test.PulseTestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrioritiserTest extends PulseTestCase
{
    private Prioritiser prioritiser;
    private Project project;

    public void setUp()
    {
        project = mock(Project.class);
        BuildResult buildResult = mock(BuildResult.class);
        stub(buildResult.getProject()).toReturn(project);

        RecipeAssignmentRequest request = new RecipeAssignmentRequest(project, null, null, null, null, buildResult);
        prioritiser = new Prioritiser(request);
    }

    public void testAgentNameSignficant()
    {
        Agent agent1 = mock(Agent.class);
        Agent agent2 = mock(Agent.class);
        stub(project.getName()).toReturn("project name");
        stub(agent1.getName()).toReturn("foo");
        stub(agent2.getName()).toReturn("bar");

        assertFalse(0 == prioritiser.compare(agent1, agent2));
    }
    
    public void testCompareSameAgent()
    {
        Agent agent = mock(Agent.class);
        stub(agent.getName()).toReturn("foo");

        assertTrue(0 == prioritiser.compare(agent, agent));
    }

    public void testProjectNameSignficant()
    {
        verifyDifferentLists("project1", "project2");
    }

    public void verifyDifferentLists(String projectName1, String projectName2)
    {
        List<Agent> agents = new ArrayList<Agent>(10);
        for (int i = 0; i < 10; i++)
        {
            Agent agent = mock(Agent.class);
            stub(agent.getName()).toReturn("agent" + i);
            agents.add(agent);
        }

        List<Agent> agentListProject1 = sortList(projectName1, agents);
        List<Agent> agentListProject2 = sortList(projectName2, agents);

        assertFalse(agentListProject1.equals(agentListProject2));
    }

    private List<Agent> sortList(String projectName1, List<Agent> agents)
    {
        stub(project.getName()).toReturn(projectName1);
        List<Agent> agentListProject1 = new ArrayList<Agent>(agents);
        Collections.sort(agentListProject1, prioritiser);
        return agentListProject1;
    }

    public void testComparatorTotalOrdering()
    {
        Agent agents[] = new Agent[4];
        for (int i = 0; i < agents.length; i++)
        {
            agents[i] = mock(Agent.class);
            stub(agents[i].getName()).toReturn("agent" + i);
        }
        
        for (Agent agent1 : agents)
        {
            // test irreflexive.
            assertTrue(prioritiser.compare(agent1, agent1) == 0);
            for (Agent agent2 : agents)
            {

                // test symmetry of equals.
                if (prioritiser.compare(agent1, agent2) == 0)
                {
                    assertTrue(prioritiser.compare(agent2, agent1) == 0);
                }
                // test asymmetry of inequality.
                else if (prioritiser.compare(agent1, agent2) < 0)
                {
                    assertFalse(prioritiser.compare(agent2, agent1) < 0);
                }
                // need this too in case compare is always positive.
                else if (prioritiser.compare(agent1, agent2) > 0)
                {
                    assertFalse(prioritiser.compare(agent2, agent1) > 0);
                }

                // test transitive.
                for (Agent agent3 : agents)
                {
                    if (prioritiser.compare(agent1, agent2) < 0 && prioritiser.compare(agent2, agent3) < 0)
                    {
                        assertTrue(prioritiser.compare(agent1, agent3) < 0);
                    }
                    else if (prioritiser.compare(agent1, agent2) > 0 && prioritiser.compare(agent2, agent3) > 0)
                    {
                        assertTrue(prioritiser.compare(agent1, agent3) > 0);
                    }
                }
            }
        }
    }

}
