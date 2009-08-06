package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.Project.Command.CAPTURES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectReportsPage;
import com.zutubi.pulse.core.commands.api.FileOutputConfiguration;
import com.zutubi.pulse.core.commands.core.CustomFieldConfiguration;
import com.zutubi.pulse.core.commands.core.CustomFieldsCommandConfiguration;
import com.zutubi.pulse.core.engine.api.FieldScope;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public class ProjectReportsAcceptanceTest extends SeleniumTestBase
{
    private static final String METRIC1 = "metric1";
    private static final String METRIC2 = "metric2";

    private static final String REPORT_GROUP_BUILD_TRENDS = "build trends";
    private static final String REPORT_GROUP_TEST_TRENDS = "test trends";

    private static final String REPORT_BUILD_RESULTS = "build results";

    private static final String MESSAGE_NO_BUILDS = "No builds found";
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
        xmlRpcHelper.loginAsAdmin();
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
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
        String projectPath = addProject(random, true);
        String capturesPath = getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE, COMMANDS, DEFAULT_COMMAND, CAPTURES);

        Hashtable<String, Object> capture = xmlRpcHelper.createEmptyConfig(FileOutputConfiguration.class);
        capture.put(Constants.Project.Command.FileOutput.NAME, "fields");
        capture.put(Constants.Project.Command.FileOutput.FILE, propertiesFile.getAbsolutePath().replace('\\', '/'));
        capture.put(Constants.Project.Command.FileOutput.POSTPROCESSORS, new Vector<String>(asList(getPath(projectPath, "postProcessors", "custom field processor"))));

        xmlRpcHelper.insertConfig(capturesPath, capture);
        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();

        BuildDetailedViewPage detailedViewPage = browser.openAndWaitFor(BuildDetailedViewPage.class, random, buildId);
        detailedViewPage.clickStage(STAGE_DEFAULT);
        browser.waitForElement(detailedViewPage.getCustomFieldsId(STAGE_DEFAULT));
        assertEquals(Double.toString(value1), detailedViewPage.getCustomFieldValue(STAGE_DEFAULT, 1));
        assertEquals(Double.toString(value2), detailedViewPage.getCustomFieldValue(STAGE_DEFAULT, 2));
    }

    public void testBuildScopedCustomFields() throws Exception
    {
        final String FIELD_NAME = "test field";
        final String FIELD_VALUE = "3";

        String projectPath = addProject(random, true);
        String commandsPath = getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE, COMMANDS);

        Hashtable<String, Object> field = xmlRpcHelper.createEmptyConfig(CustomFieldConfiguration.class);
        field.put(Constants.Project.CustomFieldsCommand.Field.NAME, FIELD_NAME);
        field.put(Constants.Project.CustomFieldsCommand.Field.VALUE, FIELD_VALUE);
        field.put(Constants.Project.CustomFieldsCommand.Field.SCOPE, FieldScope.BUILD.toString());

        Hashtable<String, Object> fields = new Hashtable<String, Object>();
        fields.put(FIELD_NAME, field);

        Hashtable<String, Object> command = xmlRpcHelper.createEmptyConfig(CustomFieldsCommandConfiguration.class);
        command.put(Constants.Project.Command.NAME, "fields");
        command.put(Constants.Project.CustomFieldsCommand.FIELDS, fields);

        xmlRpcHelper.insertConfig(commandsPath, command);
        long buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();

        BuildDetailedViewPage detailedViewPage = browser.openAndWaitFor(BuildDetailedViewPage.class, random, buildId);
        browser.waitForElement(detailedViewPage.getCustomFieldsId(null));
        assertEquals(FIELD_VALUE, detailedViewPage.getCustomFieldValue(null, 1));
    }

    public void testReportsSanity() throws Exception
    {
        String projectPath = addProject(random, true);
        String reportGroupsPath = getPath(projectPath, Constants.Project.REPORT_GROUPS);

        String buildTrendsPath = getPath(reportGroupsPath, REPORT_GROUP_BUILD_TRENDS);
        String testTrendsPath = getPath(reportGroupsPath, REPORT_GROUP_TEST_TRENDS);
        xmlRpcHelper.deleteConfig(buildTrendsPath);
        xmlRpcHelper.deleteConfig(testTrendsPath);

        loginAsAdmin();

        // Check no groups.
        ProjectReportsPage reportsPage = browser.openAndWaitFor(ProjectReportsPage.class, random);
        assertTextPresent(MESSAGE_NO_GROUPS);

        xmlRpcHelper.restoreConfig(buildTrendsPath);
        xmlRpcHelper.restoreConfig(testTrendsPath);

        // No group name in the url leads to the first group
        reportsPage.openAndWaitFor();
        reportsPage = browser.createPage(ProjectReportsPage.class, random, REPORT_GROUP_BUILD_TRENDS);
        assertTrue(reportsPage.isPresent());

        // Go away and come back to the full url (with group), check handling of
        // no builds.
        browser.open(urls.base());
        reportsPage.openAndWaitFor();
        assertTextPresent(MESSAGE_NO_BUILDS);

        // Check that a build gives some data to report.
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        reportsPage.openAndWaitFor();
        assertTextNotPresent(MESSAGE_NO_BUILDS);

        checkReportData();

        reportsPage = browser.openAndWaitFor(ProjectReportsPage.class, random, REPORT_GROUP_TEST_TRENDS);

        // Check that clicking apply preserves the visible group.
        reportsPage.clickApply();
        reportsPage.waitFor();
    }

    @SuppressWarnings({"unchecked"})
    private void checkReportData() throws Exception
    {
        Hashtable<String, Object> reportData = xmlRpcHelper.getReportData(random, REPORT_GROUP_BUILD_TRENDS, REPORT_BUILD_RESULTS, 1, "builds");
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
