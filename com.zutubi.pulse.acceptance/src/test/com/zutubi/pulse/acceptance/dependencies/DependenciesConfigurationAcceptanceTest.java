package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.SeleniumTestBase;
import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectDependenciesPage;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import static com.zutubi.util.CollectionUtils.asPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * A set of acceptance tests focused on the dependency systems UI.
 */
public class DependenciesConfigurationAcceptanceTest extends SeleniumTestBase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        xmlRpcHelper.logout();

        super.tearDown();
    }

    public void testDependencyCanNotReferenceOwningProject() throws Exception
    {
        String projectName = random;

        // create project
        String path = addProject(projectName, true);

        String projectHandle = xmlRpcHelper.getConfigHandle(path);

        loginAsAdmin();

        // go to add dependency page.
        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, projectName, false);

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

        addProject(projectA, true);
        addProject(projectB, true);
        addProject(projectC, true);

        addStages(projectA, "a1", "a2", "a3", "a4");
        addStages(projectB, "b1", "b2");

        loginAsAdmin();

        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, projectC, false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd(); // takes us to the wizard version of the dependency form.
        form.waitFor();

        // We expect no initial stage option values since no project is selected.
        List<String> optionValues = form.getStagesOptionValues();
        assertEquals(0, optionValues.size());

        assertExpectedStageOptions(form, projectA, "a1", "a2", "a3", "a4", "default");
        assertExpectedStageOptions(form, projectB, "b1", "b2", "default");

        form.finishNamedFormElements(
                asPair("project", getProjectHandle(projectB)),
                asPair("allStages", "false"),
                asPair("stages", getStageHandles(projectB, "b1")[0])
        );
        projectDependenciesPage.waitFor();

        assertDependenciesTableRow(projectDependenciesPage, 1, projectB, "latest.integration", "b1", "true");

        Vector<String> listing = xmlRpcHelper.getConfigListing("projects/" + projectC + "/dependencies/dependencies");
        String dependencyName = listing.get(0);

        form = projectDependenciesPage.clickView(dependencyName);
        form.waitFor();
        
        assertExpectedStageOptions(form, projectA, "a1", "a2", "a3", "a4", "default");
        form.saveNamedFormElements(
                asPair("project", getProjectHandle(projectA)),
                asPair("allStages", "false"),
                asPair("stages", getStageHandles(projectA, "a1")[0])
        );
        projectDependenciesPage.waitFor();

        assertDependenciesTableRow(projectDependenciesPage, 1, projectA, "latest.integration", "a1", "true");
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
        assertEquals(expectedStages.length, optionValues.size());
        assertThat(optionValues, hasItems(getStageHandles(projectName, expectedStages)));
    }

    private String getProjectHandle(String name) throws Exception
    {
        return xmlRpcHelper.getConfigHandle("projects/" + name);
    }

    private String[] getStageHandles(String projectName, String... stageNames) throws Exception
    {
        List<String> handles = new LinkedList<String>();
        for (String stageName : stageNames)
        {
            handles.add(xmlRpcHelper.getConfigHandle("projects/" + projectName + "/stages/" + stageName));
        }
        return handles.toArray(new String[handles.size()]);
    }

    private void addStages(String projectName, String... stageNames) throws Exception
    {
        for (String stageName : stageNames)
        {
            Hashtable<String, Object> stage = xmlRpcHelper.createDefaultConfig(BuildStageConfiguration.class);
            stage.put("name", stageName);
            xmlRpcHelper.insertConfig("projects/" + projectName + "/stages", stage);
        }
    }
}
