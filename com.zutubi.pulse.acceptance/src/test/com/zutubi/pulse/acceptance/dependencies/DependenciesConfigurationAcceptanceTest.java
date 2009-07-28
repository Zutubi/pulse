package com.zutubi.pulse.acceptance.dependencies;

import com.zutubi.pulse.acceptance.SeleniumTestBase;
import com.zutubi.pulse.acceptance.forms.admin.DependencyForm;
import com.zutubi.pulse.acceptance.pages.admin.ProjectConfigPage;
import com.zutubi.pulse.acceptance.pages.admin.ProjectDependenciesPage;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import static com.zutubi.util.CollectionUtils.asPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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

        assertThat(form.getProjectOptions(), hasItem(projectHandle));
    }

    public void testDependencyStageOptionsBelongToSelectedProject() throws Exception
    {
        // two projects. Project A will have 5 stages, Project B will have 3 stages, Project C will
        // depend on A or B.
        String projectA = random + "A";
        String projectB = random + "B";
        String projectC = random + "C";

        addProject(projectA, true);
        addProject(projectB, true);
        addProject(projectC, true);

        addStages(projectA, "1", "2", "3", "4");
        addStages(projectB, "a", "b");

        String projectAHandle = getProjectHandle(projectA);
        String projectBHandle = getProjectHandle(projectB);

        loginAsAdmin();

        ProjectConfigPage projectPage = browser.openAndWaitFor(ProjectConfigPage.class, projectC, false);

        ProjectDependenciesPage projectDependenciesPage = projectPage.clickDependenciesAndWait();
        DependencyForm form = projectDependenciesPage.clickAdd(); // takes us to the wizard version of the dependency form.
        form.waitFor();

        // We expect no initial stage option values since no project is selected.
        List<String> optionValues = form.getStagesOptionValues();
        assertEquals(0, optionValues.size());

        form.setProject(projectAHandle);
        optionValues = form.getStagesOptionValues();
        assertEquals(5, optionValues.size());
        assertThat(optionValues, hasItems(getStageHandles(projectA, "1", "2", "3", "4", "default")));

        form.setProject(projectBHandle);
        optionValues = form.getStagesOptionValues();
        assertEquals(3, optionValues.size());
        String[] projectBStageHandles = getStageHandles(projectB, "a", "b", "default");
        assertThat(optionValues, hasItems(projectBStageHandles));

        form.finishNamedFormElements(
                asPair("project", projectBHandle), 
                asPair("allStages", "false"),
                asPair("stages", projectBStageHandles[1])
        );

        // verify that the dependency appears as expected, with stage 'b' selected.

        // Hmmzz, finding the path to the newly created dependency instance is problematic.  In particular,
        // it is projects/projectName/dependencies/dependencies/xyz, where xyz is that dependency instances
        // name.  However, that name is not available on the instance itself, nor from the dependencies list
        // in the DependenciesConfiguration instance.
        // TODO: check form (not just wizard as above).
        //projectDependenciesPage.clickView(projectBHandle);
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
