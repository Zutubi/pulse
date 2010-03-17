package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.UnknownBuildReason;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import static org.mockito.Mockito.mock;

import static java.util.Arrays.asList;

public class ProjectSubscriptionConfigurationTest extends PulseTestCase
{
    private static final String LABEL_EVEN = "even";
    private static final String LABEL_ODD = "odd";

    private Project project1;
    private Project project2;
    private Project project3;

    private ProjectSubscriptionConfiguration suscription;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        project1 = createProject(1);
        project2 = createProject(2);
        project3 = createProject(3);

        suscription = new ProjectSubscriptionConfiguration();
        suscription.setAllProjects(true);
        suscription.setCondition(new AllBuildsConditionConfiguration());
        suscription.setConfigurationProvider(mock(ConfigurationProvider.class));
        suscription.setNotifyConditionFactory(new NotifyConditionFactory());
    }

    private Project createProject(long id)
    {
        Project project = new Project();
        project.setId(id);
        ProjectConfiguration config = new ProjectConfiguration("p" + id);
        if ((id % 2) == 0)
        {
            config.getLabels().add(new LabelConfiguration(LABEL_EVEN));
        }
        else
        {
            config.getLabels().add(new LabelConfiguration(LABEL_ODD));
        }
        config.setProjectId(id);
        project.setConfig(config);
        return project;
    }

    public void testAllProjects()
    {
        assertTrue(suscription.conditionSatisfied(createBuild(project1)));
        assertTrue(suscription.conditionSatisfied(createBuild(project2)));
        assertTrue(suscription.conditionSatisfied(createBuild(project3)));
    }

    public void testNoProjects()
    {
        suscription.setAllProjects(false);

        assertFalse(suscription.conditionSatisfied(createBuild(project1)));
        assertFalse(suscription.conditionSatisfied(createBuild(project2)));
        assertFalse(suscription.conditionSatisfied(createBuild(project3)));
    }

    public void testSpecificProjects()
    {
        suscription.setAllProjects(false);
        suscription.setProjects(asList(project1.getConfig(), project3.getConfig()));

        assertTrue(suscription.conditionSatisfied(createBuild(project1)));
        assertFalse(suscription.conditionSatisfied(createBuild(project2)));
        assertTrue(suscription.conditionSatisfied(createBuild(project3)));
    }

    public void testByLabel()
    {
        suscription.setAllProjects(false);
        suscription.setLabels(asList(LABEL_EVEN));

        assertFalse(suscription.conditionSatisfied(createBuild(project1)));
        assertTrue(suscription.conditionSatisfied(createBuild(project2)));
        assertFalse(suscription.conditionSatisfied(createBuild(project3)));
    }

    public void testByProjectAndLabel()
    {
        suscription.setAllProjects(false);
        suscription.setProjects(asList(project2.getConfig()));
        suscription.setLabels(asList(LABEL_ODD));

        assertTrue(suscription.conditionSatisfied(createBuild(project1)));
        assertTrue(suscription.conditionSatisfied(createBuild(project2)));
        assertTrue(suscription.conditionSatisfied(createBuild(project3)));
    }

    private BuildResult createBuild(Project project)
    {
        return new BuildResult(new UnknownBuildReason(), project, 1, false);
    }
}
