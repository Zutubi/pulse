package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.DirectoryArtifactConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests for the remote API, primarily the reporting functionality.
 * Configration functions are tested in {@link ConfigXmlRpcAcceptanceTest}.
 */
public class ReportingXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String PROJECT_HIERARCHY_PREFIX = "reporting-xmlrpc-hierarchy";
    private static final String PROJECT_HIERARCHY_TEMPLATE = PROJECT_HIERARCHY_PREFIX + "-template";
    private static final String PROJECT_HIERARCHY_CHILD1 = PROJECT_HIERARCHY_PREFIX + "-child1";
    private static final String PROJECT_HIERARCHY_CHILD2 = PROJECT_HIERARCHY_PREFIX + "-child2";

    private static final int BUILD_TIMEOUT = 90000;
    private static final int REQUEST_TIMEOUT = 5000;

    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();
        super.tearDown();
    }

    public void testGetServerInfo() throws Exception
    {
        Hashtable<String, String> info = xmlRpcHelper.getServerInfo();
        assertTrue(info.containsKey("os.name"));
        assertTrue(info.containsKey("pulse.version"));
        assertTrue(info.containsKey("user.timezone"));
        assertTrue(info.containsKey("current.time"));
    }

    public void testGetAllUserLogins() throws Exception
    {
        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return xmlRpcHelper.getAllUserLogins();
            }

            public void add(String name) throws Exception
            {
                xmlRpcHelper.insertTrivialUser(name);
            }
        });
    }

    public void testGetAllProjectNames() throws Exception
    {
        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return xmlRpcHelper.getAllProjectNames();
            }

            public void add(String name) throws Exception
            {
                xmlRpcHelper.insertSimpleProject(name, false);
            }
        });
    }

    public void testGetAllAgentNamesDoesNotIncludeTemplates() throws Exception
    {
        Vector<String> allAgents = xmlRpcHelper.getAllAgentNames();
        assertFalse(allAgents.contains(AgentManager.GLOBAL_AGENT_NAME));
    }

    public void testGetAllAgentNames() throws Exception
    {
        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return xmlRpcHelper.getAllAgentNames();
            }

            public void add(String name) throws Exception
            {
                xmlRpcHelper.insertSimpleAgent(name);
            }
        });
    }

    public void testGetAllProjectNamesDoesNotIncludeTemplates() throws Exception
    {
        Vector<String> allProjects = xmlRpcHelper.getAllProjectNames();
        assertFalse(allProjects.contains(ProjectManager.GLOBAL_PROJECT_NAME));
    }

    public void testGetMyProjectNamesAllProjects() throws Exception
    {
        Vector<String> allProjects = xmlRpcHelper.getAllProjectNames();
        Vector<String> myProjects = xmlRpcHelper.getMyProjectNames();
        Sort.StringComparator c = new Sort.StringComparator();
        Collections.sort(allProjects, c);
        Collections.sort(myProjects, c);
        assertEquals(allProjects, myProjects);
    }

    public void testGetMyProjectNames() throws Exception
    {
        String random = randomName();
        String login = random + "-user";
        String project = random + "-project";

        String userPath = xmlRpcHelper.insertTrivialUser(login);
        String projectPath = xmlRpcHelper.insertSimpleProject(project, false);

        xmlRpcHelper.logout();
        xmlRpcHelper.login(login, "");
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardSettings = xmlRpcHelper.getConfig(dashboardPath);
        dashboardSettings.put("showAllProjects", false);
        dashboardSettings.put("shownProjects", new Vector<String>(asList(projectPath)));
        xmlRpcHelper.saveConfig(dashboardPath, dashboardSettings, true);

        Vector<String> myProjects = xmlRpcHelper.getMyProjectNames();
        assertEquals(1, myProjects.size());
        assertEquals(project, myProjects.get(0));
    }

    public void testGetMyProjectNamesGroupsFiltered() throws Exception
    {
        String random = randomName();
        String login = random + "-user";
        String project1 = random + "-project-1";
        String project2 = random + "-project-2";

        String userPath = xmlRpcHelper.insertTrivialUser(login);

        // First project has label
        String project1Path = xmlRpcHelper.insertSimpleProject(project1, false);
        Hashtable<String, Object> labelConfig = xmlRpcHelper.createEmptyConfig(LabelConfiguration.class);
        labelConfig.put("label", random);
        xmlRpcHelper.insertConfig(PathUtils.getPath(project1Path, Constants.Project.LABELS), labelConfig);

        // Second has no label
        xmlRpcHelper.insertSimpleProject(project2, false);
        xmlRpcHelper.logout();

        xmlRpcHelper.login(login, "");
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardSettings = xmlRpcHelper.getConfig(dashboardPath);
        dashboardSettings.put("showAllGroups", false);
        dashboardSettings.put("showUngrouped", false);
        dashboardSettings.put("shownGroups", new Vector<String>(asList(random)));
        xmlRpcHelper.saveConfig(dashboardPath, dashboardSettings, true);

        Vector<String> myProjects = xmlRpcHelper.getMyProjectNames();
        assertEquals(1, myProjects.size());
        assertEquals(project1, myProjects.get(0));
    }

    public void testGetAllProjectGroups() throws Exception
    {
        String projectName = randomName() + "-project";
        final String projectPath = xmlRpcHelper.insertSimpleProject(projectName, false);

        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return xmlRpcHelper.getAllProjectGroups();
            }

            public void add(String name) throws Exception
            {
                String labelsPath = PathUtils.getPath(projectPath, "labels");
                Hashtable<String, Object> label = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
                label.put("label", name);
                xmlRpcHelper.insertConfig(labelsPath, label);
            }
        });
    }

    public void testGetProjectGroup() throws Exception
    {
        String random = randomName();
        String projectName = random + "-project";
        String labelName = random + "-label";

        String projectPath = xmlRpcHelper.insertSimpleProject(projectName, false);
        String labelsPath = PathUtils.getPath(projectPath, "labels");
        Hashtable<String, Object> label = xmlRpcHelper.createDefaultConfig(LabelConfiguration.class);
        label.put("label", labelName);
        xmlRpcHelper.insertConfig(labelsPath, label);

        Hashtable<String, Object> group = xmlRpcHelper.getProjectGroup(labelName);
        assertEquals(labelName, group.get("name"));
        Vector<String> projects = (Vector<String>) group.get("projects");
        assertEquals(1, projects.size());
        assertEquals(projectName, projects.get(0));
    }

    public void testGetProjectGroupNonExistant() throws Exception
    {
        // Groups are virtual: if you ask for a non-existant label, it is
        // just an empty group
        String testName = "something that does not exist";
        Hashtable<String, Object> group = xmlRpcHelper.getProjectGroup(testName);
        assertEquals(testName, group.get("name"));
        Vector<String> projects = (Vector<String>) group.get("projects");
        assertEquals(0, projects.size());
    }

    public void testGetBuild() throws Exception
    {
        // A bit of a sanity check: in reality we use this method for other
        // tests that run builds so it is exercised in a few ways.
        String projectName = randomName();
        insertSimpleProject(projectName);

        xmlRpcHelper.triggerBuild(projectName);

        Hashtable<String, Object> build;
        do
        {
            build = xmlRpcHelper.getBuild(projectName, 1);
        }
        while (build == null || !Boolean.TRUE.equals(build.get("completed")));

        assertEquals(1, build.get("id"));
        assertEquals(projectName, build.get("project"));
        assertEquals("success", build.get("status"));
    }

    public void testErrorAndWarningCounts() throws Exception
    {
        String projectName = randomName();
        Hashtable<String, Object> customType = xmlRpcHelper.createDefaultConfig(CustomTypeConfiguration.class);
        customType.put("pulseFileString", IOUtils.inputStreamToString(getInput("xml")));

        xmlRpcHelper.insertProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, xmlRpcHelper.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), customType);
        int number = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        Hashtable<String, Object> build = xmlRpcHelper.getBuild(projectName, number);
        assertNotNull(build);
        assertEquals(4, build.get("errorCount"));
        assertEquals(1, build.get("warningCount"));

        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        Hashtable<String, Object> stage = stages.get(0);
        assertEquals(3, stage.get("errorCount"));
        assertEquals(1, stage.get("warningCount"));
    }

    public void testGetBuildUnknownProject()
    {
        try
        {
            xmlRpcHelper.getBuild("this is a made up project", 1);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Unknown project 'this is a made up project'"));
        }
    }

    public void testGetBuildUnknownBuild() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        assertNull(xmlRpcHelper.getBuild(projectName, 1));
    }

    public void testDeleteBuild() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        assertTrue(xmlRpcHelper.deleteBuild(projectName, 1));
        assertNull(xmlRpcHelper.getBuild(projectName, 1));
    }

    public void testDeleteBuildUknownProject() throws Exception
    {
        try
        {
            xmlRpcHelper.deleteBuild("this is a made up project", 1);
            fail();
        }
        catch (Exception e)
        {
            assertTrue(e.getMessage().contains("Unknown project 'this is a made up project'"));
        }
    }

    public void testDeleteBuildUnknownBuild() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        assertFalse(xmlRpcHelper.deleteBuild(projectName, 1));
    }

    public void testTriggerBuildWithProperties() throws Exception
    {
        final String projectName = randomName();
        insertSimpleProject(projectName);
        xmlRpcHelper.insertProjectProperty(projectName, "existing.property", "existing value");

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("existing.property", "overriding value");
        properties.put("new.property", "new value");

        int number = xmlRpcHelper.getNextBuildNumber(projectName);
        xmlRpcHelper.triggerBuild(projectName, "", properties);
        xmlRpcHelper.waitForBuildToComplete(projectName, number, BUILD_TIMEOUT);

        Vector<Hashtable<String, Object>> artifacts = xmlRpcHelper.getArtifactsInBuild(projectName, number);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return artifact.get("name").equals("environment");
            }
        });

        assertNotNull(artifact);
        String permalink = (String) artifact.get("permalink");
        String text = downloadAsAdmin(baseUrl + permalink.substring(1) + "env.txt");

        assertThat(text, containsString("PULSE_EXISTING_PROPERTY=overriding value"));
        assertThat(text, containsString("PULSE_NEW_PROPERTY=new value"));
    }

    public void testGetLatestBuildsForProject() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        int number = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        Vector<Hashtable<String, Object>> builds = xmlRpcHelper.getLatestBuildsForProject(projectName, true, 10);
        assertEquals(1, builds.size());
        assertEquals(number, builds.get(0).get("id"));
    }

    public void testGetLatestBuildsForProjectUnknownProject() throws Exception
    {
        try
        {
            xmlRpcHelper.getLatestBuildsForProject("thereisnosuchproject", true, 10);
            fail("Can't get latest builds for unknown project");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Unknown project"));
        }
    }

    public void testGetLatestBuildsForProjectTemplate() throws Exception
    {
        ensureProjectHierarchy();

        Vector<Hashtable<String, Object>> builds = xmlRpcHelper.getLatestBuildsForProject(PROJECT_HIERARCHY_TEMPLATE, true, 10);
        assertEquals(2, builds.size());

        Hashtable<String, Object> build = builds.get(0);
        assertEquals(1, build.get("id"));
        assertEquals(PROJECT_HIERARCHY_CHILD2, build.get("project"));

        build = builds.get(1);
        assertEquals(1, build.get("id"));
        assertEquals(PROJECT_HIERARCHY_CHILD1, build.get("project"));
    }

    public void testQueryBuildsForProject() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        int number = xmlRpcHelper.runBuild(projectName, BUILD_TIMEOUT);

        Vector<Hashtable<String, Object>> builds = xmlRpcHelper.queryBuildsForProject(projectName, new Vector<String>(), -1, 10, true);
        assertEquals(1, builds.size());
        assertEquals(number, builds.get(0).get("id"));
    }

    public void testQueryBuildsForProjectUnknownProject() throws Exception
    {
        try
        {
            xmlRpcHelper.queryBuildsForProject("iamnothere", new Vector<String>(), -1, 10, true);
            fail("Can't query builds for unknown project");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Unknown project"));
        }
    }

    public void testQueryBuildsForProjectTemplate() throws Exception
    {
        ensureProjectHierarchy();

        Vector<Hashtable<String, Object>> builds = xmlRpcHelper.queryBuildsForProject(PROJECT_HIERARCHY_TEMPLATE, new Vector<String>(), -1, 10, true);
        assertEquals(2, builds.size());

        Hashtable<String, Object> build = builds.get(0);
        assertEquals(1, build.get("id"));
        assertEquals(PROJECT_HIERARCHY_CHILD2, build.get("project"));

        build = builds.get(1);
        assertEquals(1, build.get("id"));
        assertEquals(PROJECT_HIERARCHY_CHILD1, build.get("project"));
    }


    public void testGetLatestBuildsWithWarningsNoWarnings() throws Exception
    {
        ensureProjectHierarchy();

        // No builds have warnings, but this sanity check found a login bug.
        Vector<Hashtable<String, Object>> builds = xmlRpcHelper.getLatestBuildsWithWarnings(PROJECT_HIERARCHY_TEMPLATE, 10);
        assertEquals(0, builds.size());
    }

    public void testGetArtifactFileListing() throws Exception
    {
        String project = randomName();
        Hashtable<String, Object> svnConfig = xmlRpcHelper.getSubversionConfig(Constants.TEST_ANT_REPOSITORY);
        Hashtable<String, Object> antConfig = xmlRpcHelper.getAntConfig();
        antConfig.put(Constants.Project.AntType.TARGETS, "test");

        String projectPath = xmlRpcHelper.insertProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, svnConfig, antConfig);

        Hashtable<String, Object> artifactConfig = xmlRpcHelper.createDefaultConfig(DirectoryArtifactConfiguration.class);
        artifactConfig.put(Constants.DirectoryArtifact.NAME, "reports");
        artifactConfig.put(Constants.DirectoryArtifact.BASE, "build/reports");
        xmlRpcHelper.insertConfig(PathUtils.getPath(projectPath, Constants.Project.TYPE, Constants.Project.AntType.ARTIFACTS), artifactConfig);

        int buildId = xmlRpcHelper.runBuild(project, BUILD_TIMEOUT);

        Vector<String> files = xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "reports", "");
        Collections.sort(files, new Sort.StringComparator());
        assertEquals(asList(
                "html/allclasses-frame.html",
                "html/all-tests.html",
                "html/alltests-errors.html",
                "html/alltests-fails.html",
                "html/com/zutubi/testant/0_UnitTest.html",
                "html/com/zutubi/testant/0_UnitTest-fails.html",
                "html/com/zutubi/testant/package-frame.html",
                "html/com/zutubi/testant/package-summary.html",
                "html/index.html",
                "html/overview-frame.html",
                "html/overview-summary.html",
                "html/stylesheet.css",
                "TEST-com.zutubi.testant.UnitTest.xml",
                "xml/TESTS-TestSuites.xml"
        ), files);

        files = xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "reports", "html/com/zutubi/testant");
        Collections.sort(files, new Sort.StringComparator());
        assertEquals(asList("0_UnitTest.html", "0_UnitTest-fails.html", "package-frame.html", "package-summary.html"), files);

        assertEquals(Arrays.<String>asList(), xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "reports", "xm"));
        assertEquals(asList("TESTS-TestSuites.xml"), xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "reports", "xml"));
        assertEquals(asList("TESTS-TestSuites.xml"), xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "reports", "xml/"));

        try
        {
            xmlRpcHelper.getArtifactFileListing(project, buildId, "nosuchstage", "build", "reports", "");
            fail("Shouldn't work for invalid stage name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("build '1' does not have a stage named 'nosuchstage'"));
        }

        try
        {
            xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "nosuchcommand", "reports", "");
            fail("Shouldn't work for invalid command name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("build '1' stage 'default' does not have a command named 'nosuchcommand'"));
        }

        try
        {
            xmlRpcHelper.getArtifactFileListing(project, buildId, "default", "build", "nosuchartifact", "");
            fail("Shouldn't work for invalid artifact name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("build '1' stage 'default' command 'build' does not have an artifact named 'nosuchartifact'"));
        }
    }

    public void testWaitForBuildRequestToBeHandled() throws Exception
    {
        String projectName = randomName();
        Vector<String> ids = insertAndTriggerProject(projectName);

        Hashtable<String, Object> status = xmlRpcHelper.waitForBuildRequestToBeHandled(ids.get(0), REQUEST_TIMEOUT);
        assertFirstActivatedBuild(status);
        xmlRpcHelper.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testWaitForBuildRequestToBeActivated() throws Exception
    {
        String projectName = randomName();
        Vector<String> ids = insertAndTriggerProject(projectName);

        Hashtable<String, Object> status = xmlRpcHelper.waitForBuildRequestToBeActivated(ids.get(0), REQUEST_TIMEOUT);
        assertFirstActivatedBuild(status);
        xmlRpcHelper.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testGetBuildRequestStatus() throws Exception
    {
        String projectName = randomName();
        Vector<String> ids = insertAndTriggerProject(projectName);

        String id = ids.get(0);
        xmlRpcHelper.waitForBuildRequestToBeActivated(id, REQUEST_TIMEOUT);
        Hashtable<String, Object> status = xmlRpcHelper.getBuildRequestStatus(id);
        assertFirstActivatedBuild(status);
        xmlRpcHelper.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testGetBuildRequestStatusPaused() throws Exception
    {
        String projectName = randomName();
        insertSimpleProject(projectName);
        xmlRpcHelper.doConfigAction(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, projectName), ProjectConfigurationActions.ACTION_PAUSE);

        Vector<String> ids = xmlRpcHelper.triggerProjectBuild(projectName);
        String id = ids.get(0);
        xmlRpcHelper.waitForBuildRequestToBeHandled(id, REQUEST_TIMEOUT);
        Hashtable<String, Object> status = xmlRpcHelper.getBuildRequestStatus(id);
        assertEquals("REJECTED", status.get("status"));
        assertEquals("project state (paused) does not allow building", status.get("rejectionReason"));
    }

    private Vector<String> insertAndTriggerProject(String projectName) throws Exception
    {
        insertSimpleProject(projectName);
        Vector<String> ids = xmlRpcHelper.triggerProjectBuild(projectName);
        assertEquals(1, ids.size());
        return ids;
    }

    private void assertFirstActivatedBuild(Hashtable<String, Object> status)
    {
        assertEquals("ACTIVATED", status.get("status"));
        assertEquals("1", status.get("buildId"));
    }

    private void ensureProjectHierarchy() throws Exception
    {
        if (!xmlRpcHelper.configPathExists(PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, PROJECT_HIERARCHY_TEMPLATE)))
        {
            xmlRpcHelper.insertProject(PROJECT_HIERARCHY_TEMPLATE, ProjectManager.GLOBAL_PROJECT_NAME, true, xmlRpcHelper.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), xmlRpcHelper.getAntConfig());
            xmlRpcHelper.insertProject(PROJECT_HIERARCHY_CHILD1, PROJECT_HIERARCHY_TEMPLATE, false, null, null);
            xmlRpcHelper.insertProject(PROJECT_HIERARCHY_CHILD2, PROJECT_HIERARCHY_TEMPLATE, false, null, null);

            xmlRpcHelper.runBuild(PROJECT_HIERARCHY_CHILD1, BUILD_TIMEOUT);
            xmlRpcHelper.runBuild(PROJECT_HIERARCHY_CHILD2, BUILD_TIMEOUT);
        }
    }

    private void getAllHelper(GetAllHelper helper) throws Exception
    {
        String name = randomName();
        Vector<String> all = helper.get();
        int sizeBefore = all.size();
        assertFalse(all.contains(name));
        helper.add(name);
        all = helper.get();
        assertEquals(sizeBefore + 1, all.size());
        assertTrue(all.contains(name));
    }

    private interface GetAllHelper
    {
        Vector<String> get() throws Exception;

        void add(String name) throws Exception;
    }
}
