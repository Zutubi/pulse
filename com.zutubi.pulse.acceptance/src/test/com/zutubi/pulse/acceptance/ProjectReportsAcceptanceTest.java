package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildDetailedViewPage;
import com.zutubi.pulse.acceptance.pages.browse.ProjectReportsPage;
import com.zutubi.pulse.master.tove.config.project.types.FileArtifactConfiguration;
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

        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
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
        String artifactsPath = getPath(projectPath, Constants.Project.TYPE, Constants.Project.AntType.ARTIFACTS);

        Hashtable<String, Object> artifact = xmlRpcHelper.createEmptyConfig(FileArtifactConfiguration.class);
        artifact.put(Constants.FileArtifact.NAME, "fields");
        artifact.put(Constants.FileArtifact.FILE, propertiesFile.getAbsolutePath().replace('\\', '/'));
        artifact.put(Constants.FileArtifact.POSTPROCESSORS, new Vector<String>(asList("custom-fields")));

        xmlRpcHelper.insertConfig(artifactsPath, artifact);
        int buildId = xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);

        loginAsAdmin();

        BuildDetailedViewPage detailedViewPage = new BuildDetailedViewPage(selenium, urls, random, buildId);
        detailedViewPage.goTo();
        detailedViewPage.clickStage(STAGE_DEFAULT);
        SeleniumUtils.waitForElementId(selenium, detailedViewPage.getCustomFieldsId(STAGE_DEFAULT));
        assertEquals(Double.toString(value1), detailedViewPage.getCustomFieldValue(STAGE_DEFAULT, 1));
        assertEquals(Double.toString(value2), detailedViewPage.getCustomFieldValue(STAGE_DEFAULT, 2));
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
        ProjectReportsPage reportsPage = new ProjectReportsPage(selenium, urls, random, null);
        reportsPage.goTo();
        assertTextPresent(MESSAGE_NO_GROUPS);

        xmlRpcHelper.restoreConfig(buildTrendsPath);
        xmlRpcHelper.restoreConfig(testTrendsPath);

        // No group name in the url leads to the first group
        reportsPage.goTo();
        reportsPage = new ProjectReportsPage(selenium, urls, random, REPORT_GROUP_BUILD_TRENDS);
        assertTrue(reportsPage.isPresent());

        // Go away nd come back to the full url (with group), check handling of
        // no builds.
        goTo(urls.base());
        reportsPage.goTo();
        assertTextPresent(MESSAGE_NO_BUILDS);

        // Check that a build gives some data to report.
        xmlRpcHelper.runBuild(random, BUILD_TIMEOUT);
        reportsPage.goTo();
        assertTextNotPresent(MESSAGE_NO_BUILDS);

        checkReportData();

        reportsPage = new ProjectReportsPage(selenium, urls, random, REPORT_GROUP_TEST_TRENDS);
        reportsPage.goTo();

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
