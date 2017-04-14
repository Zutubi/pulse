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

import com.zutubi.pulse.acceptance.pages.browse.BuildDetailsPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectReportsPage;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.core.CustomFieldConfiguration;
import com.zutubi.pulse.core.commands.core.CustomFieldsCommandConfiguration;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard.DEFAULT_STAGE;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import static java.util.Arrays.asList;

public class ProjectReportsAcceptanceTest extends AcceptanceTestBase
{
    private static final String METRIC1 = "metric1";
    private static final String METRIC2 = "metric2";

    private static final String REPORT_GROUP_BUILD_TRENDS = "build trends";
    private static final String REPORT_GROUP_TEST_TRENDS = "test trends";

    private static final String REPORT_BUILD_RESULTS = "build results";

    private static final String MESSAGE_NO_BUILDS = "No builds found in time frame";
    private static final String MESSAGE_NO_GROUPS = "No report groups defined";

    private static final long BUILD_TIMEOUT = 90000;

    private static final String STAGE_DEFAULT = "default";

    private File tempDir;
    private File propertiesFile;
    private double value1;
    private double value2;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        tempDir = createTempDirectory();
        propertiesFile = new File(tempDir, "custom.properties");
        generateRandomMetrics();
        rpcClient.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    private void generateRandomMetrics() throws IOException
    {
        Properties random = new Properties();
        value1 = Math.random();
        random.put(METRIC1, Double.toString(value1));
        value2 = Math.random();
        random.put(METRIC2, Double.toString(value2));
        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(propertiesFile);
            random.store(os, "random");
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    public void testCustomFieldPostProcessing() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);
        String capturesPath = getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS);

        Hashtable<String, Object> capture = rpcClient.RemoteApi.createEmptyConfig(FileArtifactConfiguration.class);
        capture.put(Constants.Project.Command.FileArtifact.NAME, "fields");
        capture.put(Constants.Project.Command.FileArtifact.FILE, propertiesFile.getAbsolutePath().replace('\\', '/'));
        capture.put(Constants.Project.Command.FileArtifact.POSTPROCESSORS, new Vector<String>(asList(getPath(projectPath, "postProcessors", "custom field processor"))));

        rpcClient.RemoteApi.insertConfig(capturesPath, capture);
        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        Hashtable<String, Object> customFields = rpcClient.RemoteApi.getCustomFieldsInBuild(random, (int) buildId);
        assertEquals(new HashSet<String>(asList("", DEFAULT_STAGE)), customFields.keySet());
        assertFields(customFields, "");
        assertFields(customFields, DEFAULT_STAGE, METRIC1, Double.toString(value1), METRIC2, Double.toString(value2));

        getBrowser().loginAsAdmin();

        BuildDetailsPage detailsPage = getBrowser().openAndWaitFor(BuildDetailsPage.class, random, buildId);
        detailsPage.clickStageAndWait(STAGE_DEFAULT);
        assertEquals(Double.toString(value1), detailsPage.getCustomFieldValue(0));
        assertEquals(Double.toString(value2), detailsPage.getCustomFieldValue(1));
    }

    public void testBuildScopedCustomFields() throws Exception
    {
        final String FIELD_NAME = "test field";
        final String FIELD_VALUE = "3";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);
        String commandsPath = getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS);

        Hashtable<String, Object> field = rpcClient.RemoteApi.createEmptyConfig(CustomFieldConfiguration.class);
        field.put(Constants.Project.CustomFieldsCommand.Field.NAME, FIELD_NAME);
        field.put(Constants.Project.CustomFieldsCommand.Field.VALUE, FIELD_VALUE);
        field.put(Constants.Project.CustomFieldsCommand.Field.SCOPE, FieldScope.BUILD.toString());

        Hashtable<String, Object> fields = new Hashtable<String, Object>();
        fields.put(FIELD_NAME, field);

        Hashtable<String, Object> command = rpcClient.RemoteApi.createEmptyConfig(CustomFieldsCommandConfiguration.class);
        command.put(Constants.Project.Command.NAME, "fields");
        command.put(Constants.Project.CustomFieldsCommand.FIELDS, fields);

        rpcClient.RemoteApi.insertConfig(commandsPath, command);
        long buildId = rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);

        Hashtable<String, Object> customFields = rpcClient.RemoteApi.getCustomFieldsInBuild(random, (int) buildId);
        assertEquals(new HashSet<String>(asList("", DEFAULT_STAGE)), customFields.keySet());
        assertFields(customFields, "", FIELD_NAME, FIELD_VALUE);
        assertFields(customFields, DEFAULT_STAGE);
        
        getBrowser().loginAsAdmin();

        BuildDetailsPage detailsPage = getBrowser().openAndWaitFor(BuildDetailsPage.class, random, buildId);
        assertEquals(FIELD_VALUE, detailsPage.getCustomFieldValue(0));
    }

    public void testReportsSanity() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random);
        String reportGroupsPath = getPath(projectPath, Constants.Project.REPORT_GROUPS);

        String buildTrendsPath = getPath(reportGroupsPath, REPORT_GROUP_BUILD_TRENDS);
        String testTrendsPath = getPath(reportGroupsPath, REPORT_GROUP_TEST_TRENDS);
        rpcClient.RemoteApi.deleteConfig(buildTrendsPath);
        rpcClient.RemoteApi.deleteConfig(testTrendsPath);

        getBrowser().loginAsAdmin();

        // Check no groups.
        ProjectReportsPage reportsPage = getBrowser().openAndWaitFor(ProjectReportsPage.class, random);
        getBrowser().waitForTextPresent(MESSAGE_NO_GROUPS);

        rpcClient.RemoteApi.restoreConfig(buildTrendsPath);
        rpcClient.RemoteApi.restoreConfig(testTrendsPath);

        // No group name in the url leads to the first group
        reportsPage = getBrowser().createPage(ProjectReportsPage.class, random, REPORT_GROUP_BUILD_TRENDS);
        reportsPage.openAndWaitFor();

        // Go away and come back to the full url (with group), check handling of
        // no builds.
        getBrowser().open(urls.base());
        reportsPage.openAndWaitFor();
        getBrowser().waitForTextPresent(MESSAGE_NO_BUILDS);

        // Check that a build gives some data to report.
        rpcClient.RemoteApi.runBuild(random, BUILD_TIMEOUT);
        reportsPage.openAndWaitFor();
        assertFalse(getBrowser().isTextPresent(MESSAGE_NO_BUILDS));

        checkReportData();

        reportsPage = getBrowser().openAndWaitFor(ProjectReportsPage.class, random, REPORT_GROUP_TEST_TRENDS);

        // Check that clicking apply preserves the visible group.
        reportsPage.clickApply();
        reportsPage.waitFor();
    }

    private void assertFields(Hashtable<String, Object> allFields, String stageName, String... expected)
    {
        assertEquals("Expected an even number of varargs", 0, expected.length % 2);

        Object fields = allFields.get(stageName);
        assertNotNull("No fields for stage '" + stageName + "'", fields);
        Hashtable<String, String> expectedMap = new Hashtable<String, String>(expected.length / 2);
        for (int i = 0; i < expected.length; i+= 2)
        {
            expectedMap.put(expected[i], expected[i + 1]);
        }

        assertEquals(expectedMap, fields);
    }

    @SuppressWarnings({"unchecked"})
    private void checkReportData() throws Exception
    {
        Hashtable<String, Object> reportData = rpcClient.RemoteApi.getReportData(random, REPORT_GROUP_BUILD_TRENDS, REPORT_BUILD_RESULTS, 1, "builds");
        assertEquals(REPORT_BUILD_RESULTS, reportData.get("name"));
        Vector<Hashtable<String, Object>> seriesList = (Vector<Hashtable<String, Object>>) reportData.get("series");
        assertEquals(2, seriesList.size());

        Hashtable<String, Object> series = seriesList.get(0);
        if (!"success".equals(series.get("name")))
        {
            series = seriesList.get(1);
        }

        Vector<String> labels = (Vector<String>) series.get("labels");
        assertEquals(1, labels.size());
        // Deliberately vague check of date: within 1 day + 1 minute
        // (Constants name clash prevents usage of numerical time constants)
        assertTrue(System.currentTimeMillis() - Long.parseLong(labels.get(0)) < 1000 * 60 * 60 * 24 + 60000);

        Vector<Double> values = (Vector<Double>) series.get("values");
        assertEquals(1, values.size());
        assertEquals(1.0d, values.get(0), 0.00001);
    }
}
