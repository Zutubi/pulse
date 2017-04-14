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

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.AddProjectWizard;
import com.zutubi.pulse.acceptance.pages.admin.ProjectHierarchyPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildArtifactsPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.adt.Pair;
import com.zutubi.util.io.FileSystemUtils;
import org.openqa.selenium.By;

import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.POSTPROCESSORS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.INCLUSIONS;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * The acceptance tests for the Pulse builtin maven 1.x and 2.x integration.
 */
public class MavenAcceptanceTest extends AcceptanceTestBase
{
    private static final String COMMAND_NAME = "build";
    private static final String JUNIT_PROCESSOR_NAME = "junit xml report processor";

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

    public void testMavenDefaultTestCaptureConfiguration() throws Exception
    {
        getBrowser().loginAsAdmin();
        createMavenProject();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> capture = getCaptureConfiguration(random, "test reports");
        assertNotNull(capture);
        assertCaptureConfiguration(capture, "test reports", "target/test-reports", "TEST-*.xml", JUNIT_PROCESSOR_NAME);

        assertDefaultRequirement(random, "maven");
    }

    public void testMaven2DefaultTestArtifactConfiguration() throws Exception
    {
        getBrowser().loginAsAdmin();
        createMaven2Project();

        // We expect a artifact called surefire-reports to be configured.
        Hashtable<String, Object> capture = getCaptureConfiguration(random, "test reports");
        assertNotNull(capture);
        assertCaptureConfiguration(capture, "test reports", "target/surefire-reports", "TEST-*.xml", JUNIT_PROCESSOR_NAME);

        assertDefaultRequirement(random, "maven2");
    }

    public void testMaven2BuildPicksUpTests() throws Exception
    {
        getBrowser().loginAsAdmin();
        createMaven2Project();

        long buildNumber = runBuild(random);

        // We expect the summary page to report that 1 test passed.
        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, random, buildNumber);
        assertEquals("1 passed", summaryPage.getTestsSummary());

        // We expect the artifacts page to contain an artifact called test reports.
        BuildArtifactsPage artifactsPage = getBrowser().openAndWaitFor(BuildArtifactsPage.class, random, buildNumber);
        getBrowser().waitForElement(By.xpath(artifactsPage.getArtifactXPath("test reports")));
    }

    private void assertDefaultRequirement(String projectName, String resourceName) throws Exception
    {
        Vector<Hashtable<String, Object>> requiredResources = rpcClient.RemoteApi.getConfig(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, Constants.Project.REQUIREMENTS));
        assertEquals(1, requiredResources.size());
        Hashtable<String, Object> requirement = requiredResources.get(0);
        assertEquals(resourceName, requirement.get("resource"));
        assertEquals(true, requirement.get("defaultVersion"));
        assertEquals(true, requirement.get("optional"));
    }

    @SuppressWarnings({ "unchecked" })
    private void assertCaptureConfiguration(Hashtable<String, Object> capture, String expectedName, String expectedBase, String expectedIncludes, String... expectedPostProcessors)
    {
        Vector<String> postprocessors = (Vector<String>) capture.get(POSTPROCESSORS);
        assertEquals(expectedPostProcessors.length, postprocessors.size());
        for (int i = 0; i < expectedPostProcessors.length; i++)
        {
            assertEquals(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, random, "postProcessors", expectedPostProcessors[i]), postprocessors.get(i));
        }
        assertEquals(expectedName, capture.get(Constants.Project.NAME));
        assertEquals(expectedBase, FileSystemUtils.normaliseSeparators((String) capture.get(BASE)));
        assertEquals(expectedIncludes, ((Vector) capture.get(INCLUSIONS)).get(0));
    }

    private void createMavenProject() throws Exception
    {
        createMavenProject("zutubi.mavenCommandConfig", asPair("targets", "install"));
    }

    private void createMaven2Project() throws Exception
    {
        createMavenProject("zutubi.maven2CommandConfig", asPair("goals", "install"));
    }

    private void createMavenProject(final String commandType, final Pair<String, String>... fieldValues) throws Exception
    {
        AddProjectWizard wizard = new AddProjectWizard(getBrowser(), rpcClient.RemoteApi);
        wizard.runAddProjectWizard(new AddProjectWizard.DefaultProjectWizardDriver(ProjectManager.GLOBAL_PROJECT_NAME, random, false)
        {
            @Override
            public void scmState(AddProjectWizard.ScmState form)
            {
                form.nextNamedFormElements(asPair("url", Constants.TEST_MAVEN_REPOSITORY));
            }

            @Override
            public String selectCommand()
            {
                return commandType;
            }

            @Override
            public void commandState(AddProjectWizard.CommandState form)
            {
                @SuppressWarnings({"unchecked"})
                Pair<String, String> values[] = new Pair[fieldValues.length + 1];
                System.arraycopy(fieldValues, 0, values, 0, fieldValues.length);
                values[fieldValues.length] = new Pair<String, String>("name", COMMAND_NAME);
                form.finishNamedFormElements(values);
            }
        });

        ProjectHierarchyPage hierarchyPage = getBrowser().createPage(ProjectHierarchyPage.class, random, false);
        hierarchyPage.waitFor();
    }

    private int runBuild(String projectName) throws Exception
    {
        return rpcClient.RemoteApi.runBuild(projectName);
    }

    private Hashtable<String, Object> getCaptureConfiguration(String projectName, String artifactName) throws Exception
    {
        return rpcClient.RemoteApi.getProjectCapture(projectName, "default", COMMAND_NAME, artifactName);
    }
}
