package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
import com.zutubi.pulse.acceptance.forms.admin.TriggerBuildForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectDependenciesPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectHomePage;
import com.zutubi.pulse.acceptance.utils.DepAntProject;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurationHelper;
import com.zutubi.pulse.core.dependency.ivy.IvyLatestRevisionMatcher;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.zutubi.util.CollectionUtils.asPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * A set of acceptance tests focused on the dependency systems UI.
 */
public class DependenciesConfigurationAcceptanceTest extends AcceptanceTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.logout();

        super.tearDown();
    }

    private void insertProject(ProjectConfigurationHelper project) throws Exception
    {
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
    }

    public void testDependencyCanNotReferenceOwningProject() throws Exception
    {
        String projectName = random;

        // create project
        String path = rpcClient.RemoteApi.insertSimpleProject(projectName);

        String projectHandle = rpcClient.RemoteApi.getConfigHandle(path);

        getBrowser().loginAsAdmin();

        // go to add dependency page.
        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, projectName, false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd();
        form.waitFor();

        assertThat(form.getProjectOptions(), not(hasItem(projectHandle)));
    }

    public void testDependencyStageOptionsBelongToSelectedProject() throws Exception
    {
        String projectA = random + "A";
        String projectB = random + "B";
        String projectC = random + "C";

        rpcClient.RemoteApi.insertSimpleProject(projectA);
        rpcClient.RemoteApi.insertSimpleProject(projectB);
        rpcClient.RemoteApi.insertSimpleProject(projectC);

        addStages(projectA, "a1", "a2", "a3", "a4");
        addStages(projectB, "b1", "b2");

        getBrowser().loginAsAdmin();

        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, projectC, false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd();
        form.waitFor();

        // We expect no initial stage option values since no project is selected.
        List<String> optionValues = form.getStagesOptionValues();
        assertEquals(0, optionValues.size());

        assertExpectedStageOptions(form, projectA, "a1", "a2", "a3", "a4", "default");
        assertExpectedStageOptions(form, projectB, "b1", "b2", "default");

        form.finishNamedFormElements(
                asPair("project", getProjectHandle(projectB)),
                asPair("stageType", DependencyConfiguration.StageType.SELECTED_STAGES.name()),
                asPair("stages", getStageHandles(projectB, "b1")[0])
        );
        projectDependenciesPage.waitFor();

        assertDependenciesTableRow(projectDependenciesPage, 1, projectB, "latest.integration", "b1", "true");

        Vector<String> listing = rpcClient.RemoteApi.getConfigListing("projects/" + projectC + "/dependencies/dependencies");
        String dependencyName = listing.get(0);

        form = projectDependenciesPage.clickView(dependencyName);
        form.waitFor();

        assertExpectedStageOptions(form, projectA, "a1", "a2", "a3", "a4", "default");
        form.saveNamedFormElements(
                asPair("project", getProjectHandle(projectA)),
                asPair("stageType", DependencyConfiguration.StageType.SELECTED_STAGES.name()),
                asPair("stages", getStageHandles(projectA, "a1")[0])
        );
        projectDependenciesPage.waitFor();

        assertDependenciesTableRow(projectDependenciesPage, 1, projectA, "latest.integration", "a1", "true");
    }

    public void testCircularDependencyCheck() throws Exception
    {
        DepAntProject projectA = projectConfigurations.createDepAntProject(random + "A");
        insertProject(projectA);

        DepAntProject projectB = projectConfigurations.createDepAntProject(random + "B");
        projectB.addDependency(projectA).setTransitive(true);
        insertProject(projectB);

        DepAntProject projectC = projectConfigurations.createDepAntProject(random + "C");
        projectC.addDependency(projectB);
        insertProject(projectC);

        getBrowser().loginAsAdmin();

        ProjectConfigPage projectPage = getBrowser().openAndWaitFor(ProjectConfigPage.class, projectA.getName(), false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd(); // takes us to the wizard version of the dependency form.
        form.waitFor();

        form.finishNamedFormElements(asPair("project", String.valueOf(projectC.getConfig().getHandle())));
        form.waitFor();
        assertTrue(form.getFieldErrorMessage("project").contains("circular dependency detected"));
    }

    public void testRebuildCheckboxAppearsOnManualTrigger() throws Exception
    {
        String projectA = random + "A";
        String projectB = random + "B";

        rpcClient.RemoteApi.insertSimpleProject(projectA);
        rpcClient.RemoteApi.insertSimpleProject(projectB);
        rpcClient.RemoteApi.enableBuildPrompting(projectB);

        getBrowser().loginAsAdmin();

        assertFalse(isRebuildOptionAvailableOnPrompt(projectB, false));

        // add a dependency to the project.
        addDependency(projectB, projectA);

        assertTrue(isRebuildOptionAvailableOnPrompt(projectB, true));
    }

    private boolean isRebuildOptionAvailableOnPrompt(String projectName, boolean expected)
    {
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);
        home.triggerBuild();

        TriggerBuildForm form = getBrowser().createForm(TriggerBuildForm.class);
        if (expected)
        {
            form.expectRebuildField();
        }
        form.waitFor();
        form.waitFor();
        return form.isRebuildCheckboxPresent();
    }

    public void testRebuildOptionAvailabilityOnProjectHomePage() throws Exception
    {
        String upstreamProject1 = random + "-upstream1";
        String upstreamProject2 = random + "-upstream2";
        String downstreamIntegrationProject = random + "-downstream-integration";
        String downstreamMilestoneProject = random + "-downstream-milestone";
        String downstreamBothProject = random + "-downstream-both";

        rpcClient.RemoteApi.insertSimpleProject(upstreamProject1);
        rpcClient.RemoteApi.insertSimpleProject(upstreamProject2);

        rpcClient.RemoteApi.insertSimpleProject(downstreamIntegrationProject);
        addDependency(downstreamIntegrationProject, upstreamProject1);

        rpcClient.RemoteApi.insertSimpleProject(downstreamMilestoneProject);
        addDependency(downstreamMilestoneProject, upstreamProject1, IvyStatus.STATUS_MILESTONE);

        rpcClient.RemoteApi.insertSimpleProject(downstreamBothProject);
        addDependency(downstreamBothProject, upstreamProject1);
        addDependency(downstreamBothProject, upstreamProject2, IvyStatus.STATUS_MILESTONE);

        getBrowser().loginAsAdmin();

        assertFalse(isRebuildActionAvailableOnProjectHomePage(upstreamProject1));
        assertTrue(isRebuildActionAvailableOnProjectHomePage(downstreamIntegrationProject));
        assertFalse(isRebuildActionAvailableOnProjectHomePage(downstreamMilestoneProject));
        assertTrue(isRebuildActionAvailableOnProjectHomePage(downstreamBothProject));

        turnOnPromptOption(downstreamMilestoneProject);
        assertTrue(isRebuildActionAvailableOnProjectHomePage(downstreamMilestoneProject));
    }

    private boolean isRebuildActionAvailableOnProjectHomePage(String projectName)
    {
        ProjectHomePage home = getBrowser().openAndWaitFor(ProjectHomePage.class, projectName);
        return home.isRebuildActionPresent();
    }

    private void turnOnPromptOption(String project) throws Exception
    {
        String optionsPath = PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, project, Constants.Project.OPTIONS);
        Hashtable<String, Object> options = rpcClient.RemoteApi.getConfig(optionsPath);
        options.put("prompt", true);
        rpcClient.RemoteApi.saveConfig(optionsPath, options, false);
    }

    private void assertDependenciesTableRow(ProjectDependenciesPage page, int rowIndex, String project, String revision, String stages, String transitive)
    {
        ProjectDependenciesPage.DependencyRow row = page.getDependencyRow(rowIndex);
        assertEquals(project, row.getProjectName());
        assertEquals(revision, row.getRevision());
        assertEquals(stages, row.getStageList());
        assertEquals(transitive, row.getTransitive());
    }

    private void assertExpectedStageOptions(DependencyForm form, String projectName, String... expectedStages) throws Exception
    {
        String projectHandle = getProjectHandle(projectName);
        form.setProject(projectHandle);
        List<String> optionValues = form.getStagesOptionValues();
        // Along with the expected stages, we also expect an empty option
        assertEquals(expectedStages.length + 1, optionValues.size());
        assertEquals(optionValues.get(0), "0");
        assertThat(optionValues, hasItems(getStageHandles(projectName, expectedStages)));
    }

    private String getProjectHandle(String name) throws Exception
    {
        return rpcClient.RemoteApi.getConfigHandle("projects/" + name);
    }

    private String[] getStageHandles(String projectName, String... stageNames) throws Exception
    {
        List<String> handles = new LinkedList<String>();
        for (String stageName : stageNames)
        {
            handles.add(rpcClient.RemoteApi.getConfigHandle("projects/" + projectName + "/stages/" + stageName));
        }
        return handles.toArray(new String[handles.size()]);
    }

    private void addStages(String projectName, String... stageNames) throws Exception
    {
        for (String stageName : stageNames)
        {
            Hashtable<String, Object> stage = rpcClient.RemoteApi.createDefaultConfig(BuildStageConfiguration.class);
            stage.put("name", stageName);
            rpcClient.RemoteApi.insertConfig("projects/" + projectName + "/stages", stage);
        }
    }

    private void addDependency(String projectFrom, String projectTo) throws Exception
    {
        addDependency(projectFrom, projectTo, IvyStatus.STATUS_INTEGRATION);
    }

    private void addDependency(String projectFrom, String projectTo, String status) throws Exception
    {
        // configure the default stage.
        String projectDependenciesPath = "projects/" + projectFrom + "/dependencies";

        Hashtable<String, Object> projectDependencies = rpcClient.RemoteApi.getConfig(projectDependenciesPath);
        if (!projectDependencies.containsKey("dependencies"))
        {
            projectDependencies.put("dependencies", new Vector<Hashtable<String, Object>>());
        }

        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> dependencies = (Vector<Hashtable<String, Object>>) projectDependencies.get("dependencies");
        Hashtable<String, Object> dependency = rpcClient.RemoteApi.createEmptyConfig(DependencyConfiguration.class);
        dependency.put("project", "projects/" + projectTo);
        dependency.put("revision", IvyLatestRevisionMatcher.LATEST + status);

        dependencies.add(dependency);

        rpcClient.RemoteApi.saveConfig(projectDependenciesPath, projectDependencies, true);
    }
}
