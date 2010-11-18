package com.zutubi.pulse.acceptance;

import static com.zutubi.pulse.acceptance.Constants.Project.AntCommand.TARGETS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.ARTIFACTS;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.Artifact.NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.Command.DirectoryArtifact.BASE;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.DEFAULT_RECIPE_NAME;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.RECIPES;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.COMMANDS;
import static com.zutubi.pulse.acceptance.Constants.Project.MultiRecipeType.Recipe.DEFAULT_COMMAND;
import static com.zutubi.pulse.acceptance.Constants.Project.TYPE;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.build.queue.BuildRequestRegistry;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.types.CustomTypeConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.Sort;
import com.zutubi.util.ToStringMapping;
import com.zutubi.util.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Tests for the remote API, primarily the reporting functionality.
 * Configration functions are tested in {@link ConfigXmlRpcAcceptanceTest}.
 */
public class ReportingXmlRpcAcceptanceTest extends AcceptanceTestBase
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
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testGetServerInfo() throws Exception
    {
        Hashtable<String, String> info = rpcClient.RemoteApi.getServerInfo();
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
                return rpcClient.RemoteApi.getAllUserLogins();
            }

            public void add(String name) throws Exception
            {
                rpcClient.RemoteApi.insertTrivialUser(name);
            }
        });
    }

    public void testGetAllProjectNames() throws Exception
    {
        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return rpcClient.RemoteApi.getAllProjectNames();
            }

            public void add(String name) throws Exception
            {
                rpcClient.RemoteApi.insertSimpleProject(name, false);
            }
        });
    }

    public void testGetAllAgentNamesDoesNotIncludeTemplates() throws Exception
    {
        Vector<String> allAgents = rpcClient.RemoteApi.getAllAgentNames();
        assertFalse(allAgents.contains(AgentManager.GLOBAL_AGENT_NAME));
    }

    public void testGetAllAgentNames() throws Exception
    {
        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return rpcClient.RemoteApi.getAllAgentNames();
            }

            public void add(String name) throws Exception
            {
                rpcClient.RemoteApi.insertSimpleAgent(name);
            }
        });
    }

    public void testGetAllProjectNamesDoesNotIncludeTemplates() throws Exception
    {
        Vector<String> allProjects = rpcClient.RemoteApi.getAllProjectNames();
        assertFalse(allProjects.contains(ProjectManager.GLOBAL_PROJECT_NAME));
    }

    public void testGetMyProjectNamesAllProjects() throws Exception
    {
        Vector<String> allProjects = rpcClient.RemoteApi.getAllProjectNames();
        Vector<String> myProjects = rpcClient.RemoteApi.getMyProjectNames();
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

        String userPath = rpcClient.RemoteApi.insertTrivialUser(login);
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(project, false);

        rpcClient.logout();
        rpcClient.login(login, "");
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardSettings = rpcClient.RemoteApi.getConfig(dashboardPath);
        dashboardSettings.put("showAllProjects", false);
        dashboardSettings.put("shownProjects", new Vector<String>(asList(projectPath)));
        rpcClient.RemoteApi.saveConfig(dashboardPath, dashboardSettings, true);

        Vector<String> myProjects = rpcClient.RemoteApi.getMyProjectNames();
        assertEquals(1, myProjects.size());
        assertEquals(project, myProjects.get(0));
    }

    public void testGetMyProjectNamesGroupsFiltered() throws Exception
    {
        String random = randomName();
        String login = random + "-user";
        String project1 = random + "-project-1";
        String project2 = random + "-project-2";

        String userPath = rpcClient.RemoteApi.insertTrivialUser(login);

        // First project has label
        String project1Path = rpcClient.RemoteApi.insertSimpleProject(project1, false);
        Hashtable<String, Object> labelConfig = rpcClient.RemoteApi.createEmptyConfig(LabelConfiguration.class);
        labelConfig.put("label", random);
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(project1Path, Constants.Project.LABELS), labelConfig);

        // Second has no label
        rpcClient.RemoteApi.insertSimpleProject(project2, false);
        rpcClient.logout();

        rpcClient.login(login, "");
        String dashboardPath = PathUtils.getPath(userPath, "preferences", "dashboard");
        Hashtable<String, Object> dashboardSettings = rpcClient.RemoteApi.getConfig(dashboardPath);
        dashboardSettings.put("showAllGroups", false);
        dashboardSettings.put("showUngrouped", false);
        dashboardSettings.put("shownGroups", new Vector<String>(asList(random)));
        rpcClient.RemoteApi.saveConfig(dashboardPath, dashboardSettings, true);

        Vector<String> myProjects = rpcClient.RemoteApi.getMyProjectNames();
        assertEquals(1, myProjects.size());
        assertEquals(project1, myProjects.get(0));
    }

    public void testGetAllProjectGroups() throws Exception
    {
        String projectName = randomName() + "-project";
        final String projectPath = rpcClient.RemoteApi.insertSimpleProject(projectName, false);

        getAllHelper(new GetAllHelper()
        {
            public Vector<String> get() throws Exception
            {
                return rpcClient.RemoteApi.getAllProjectGroups();
            }

            public void add(String name) throws Exception
            {
                String labelsPath = PathUtils.getPath(projectPath, "labels");
                Hashtable<String, Object> label = rpcClient.RemoteApi.createDefaultConfig(LabelConfiguration.class);
                label.put("label", name);
                rpcClient.RemoteApi.insertConfig(labelsPath, label);
            }
        });
    }

    public void testGetProjectGroup() throws Exception
    {
        String random = randomName();
        String projectName = random + "-project";
        String labelName = random + "-label";

        String projectPath = rpcClient.RemoteApi.insertSimpleProject(projectName, false);
        String labelsPath = PathUtils.getPath(projectPath, "labels");
        Hashtable<String, Object> label = rpcClient.RemoteApi.createDefaultConfig(LabelConfiguration.class);
        label.put("label", labelName);
        rpcClient.RemoteApi.insertConfig(labelsPath, label);

        Hashtable<String, Object> group = rpcClient.RemoteApi.getProjectGroup(labelName);
        assertEquals(labelName, group.get("name"));
        @SuppressWarnings({"unchecked"})
        Vector<String> projects = (Vector<String>) group.get("projects");
        assertEquals(1, projects.size());
        assertEquals(projectName, projects.get(0));
    }

    public void testGetProjectGroupNonExistant() throws Exception
    {
        // Groups are virtual: if you ask for a non-existant label, it is
        // just an empty group
        String testName = "something that does not exist";
        Hashtable<String, Object> group = rpcClient.RemoteApi.getProjectGroup(testName);
        assertEquals(testName, group.get("name"));
        @SuppressWarnings({"unchecked"})
        Vector<String> projects = (Vector<String>) group.get("projects");
        assertEquals(0, projects.size());
    }

    public void testGetBuild() throws Exception
    {
        // A bit of a sanity check: in reality we use this method for other
        // tests that run builds so it is exercised in a few ways.
        String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);

        rpcClient.RemoteApi.triggerBuild(projectName);

        Hashtable<String, Object> build;
        do
        {
            build = rpcClient.RemoteApi.getBuild(projectName, 1);
        }
        while (build == null || !Boolean.TRUE.equals(build.get("completed")));

        assertEquals(1, build.get("id"));
        assertEquals(projectName, build.get("project"));
        assertEquals("success", build.get("status"));
        
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> stages = (Vector<Hashtable<String, Object>>) build.get("stages");
        assertEquals(1, stages.size());
        Hashtable<String, Object> stage = stages.get(0);
        assertEquals("[default]", stage.get("recipe"));
    }

    public void testErrorAndWarningCounts() throws Exception
    {
        String projectName = randomName();
        Hashtable<String, Object> customType = rpcClient.RemoteApi.createDefaultConfig(CustomTypeConfiguration.class);
        customType.put("pulseFileString", IOUtils.inputStreamToString(getInput("xml")));

        rpcClient.RemoteApi.insertProject(projectName, ProjectManager.GLOBAL_PROJECT_NAME, false, rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), customType);
        int number = rpcClient.RemoteApi.runBuild(projectName);

        Hashtable<String, Object> build = rpcClient.RemoteApi.getBuild(projectName, number);
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
            rpcClient.RemoteApi.getBuild("this is a made up project", 1);
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
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        assertNull(rpcClient.RemoteApi.getBuild(projectName, 1));
    }

    public void testDeleteBuild() throws Exception
    {
        String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        rpcClient.RemoteApi.runBuild(projectName);

        assertTrue(rpcClient.RemoteApi.deleteBuild(projectName, 1));
        assertNull(rpcClient.RemoteApi.getBuild(projectName, 1));
    }

    public void testDeleteBuildUknownProject() throws Exception
    {
        try
        {
            rpcClient.RemoteApi.deleteBuild("this is a made up project", 1);
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
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        assertFalse(rpcClient.RemoteApi.deleteBuild(projectName, 1));
    }

    public void testTriggerBuildWithProperties() throws Exception
    {
        final String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        rpcClient.RemoteApi.insertProjectProperty(projectName, "existing.property", "existing value");

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("existing.property", "overriding value");
        properties.put("new.property", "new value");

        int number = rpcClient.RemoteApi.getNextBuildNumber(projectName);
        rpcClient.RemoteApi.triggerBuild(projectName, "", properties);
        rpcClient.RemoteApi.waitForBuildToComplete(projectName, number);

        Vector<Hashtable<String, Object>> artifacts = rpcClient.RemoteApi.getArtifactsInBuild(projectName, number);
        Hashtable<String, Object> artifact = CollectionUtils.find(artifacts, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> artifact)
            {
                return artifact.get("name").equals("environment");
            }
        });

        assertNotNull(artifact);
        String permalink = (String) artifact.get("permalink");
        String text = AcceptanceTestUtils.readUriContent(baseUrl + "/" + permalink.substring(1) + "/env.txt");

        assertThat(text, containsString("PULSE_EXISTING_PROPERTY=overriding value"));
        assertThat(text, containsString("PULSE_NEW_PROPERTY=new value"));
    }

    public void testGetLatestBuildsForProject() throws Exception
    {
        String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        int number = rpcClient.RemoteApi.runBuild(projectName);

        Vector<Hashtable<String, Object>> builds = rpcClient.RemoteApi.getLatestBuildsForProject(projectName, true, 10);
        assertEquals(1, builds.size());
        assertEquals(number, builds.get(0).get("id"));
    }

    public void testGetLatestBuildsForProjectUnknownProject() throws Exception
    {
        try
        {
            rpcClient.RemoteApi.getLatestBuildsForProject("thereisnosuchproject", true, 10);
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

        Vector<Hashtable<String, Object>> builds = rpcClient.RemoteApi.getLatestBuildsForProject(PROJECT_HIERARCHY_TEMPLATE, true, 10);
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
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        int number = rpcClient.RemoteApi.runBuild(projectName);

        Vector<Hashtable<String, Object>> builds = rpcClient.RemoteApi.queryBuildsForProject(projectName, new Vector<String>(), -1, 10, true);
        assertEquals(1, builds.size());
        assertEquals(number, builds.get(0).get("id"));
    }

    public void testQueryBuildsForProjectUnknownProject() throws Exception
    {
        try
        {
            rpcClient.RemoteApi.queryBuildsForProject("iamnothere", new Vector<String>(), -1, 10, true);
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

        Vector<Hashtable<String, Object>> builds = rpcClient.RemoteApi.queryBuildsForProject(PROJECT_HIERARCHY_TEMPLATE, new Vector<String>(), -1, 10, true);
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
        Vector<Hashtable<String, Object>> builds = rpcClient.RemoteApi.getLatestBuildsWithWarnings(PROJECT_HIERARCHY_TEMPLATE, 10);
        assertEquals(0, builds.size());
    }

    public void testGetArtifactFileListing() throws Exception
    {
        String project = randomName();
        Hashtable<String, Object> svnConfig = rpcClient.RemoteApi.getSubversionConfig(Constants.TEST_ANT_REPOSITORY);
        Hashtable<String, Object> antConfig = rpcClient.RemoteApi.getAntConfig();
        antConfig.put(TARGETS, "test");

        String projectPath = rpcClient.RemoteApi.insertSingleCommandProject(project, ProjectManager.GLOBAL_PROJECT_NAME, false, svnConfig, antConfig);

        Hashtable<String, Object> artifactConfig = rpcClient.RemoteApi.createDefaultConfig(DirectoryArtifactConfiguration.class);
        artifactConfig.put(NAME, "reports");
        artifactConfig.put(BASE, "build/reports");
        rpcClient.RemoteApi.insertConfig(PathUtils.getPath(projectPath, TYPE, RECIPES, DEFAULT_RECIPE_NAME, COMMANDS, DEFAULT_COMMAND, ARTIFACTS), artifactConfig);

        int buildId = rpcClient.RemoteApi.runBuild(project);

        Vector<String> files = rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "reports", "");
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

        files = rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "reports", "html/com/zutubi/testant");
        Collections.sort(files, new Sort.StringComparator());
        assertEquals(asList("0_UnitTest.html", "0_UnitTest-fails.html", "package-frame.html", "package-summary.html"), files);

        assertEquals(Arrays.<String>asList(), rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "reports", "xm"));
        assertEquals(asList("TESTS-TestSuites.xml"), rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "reports", "xml"));
        assertEquals(asList("TESTS-TestSuites.xml"), rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "reports", "xml/"));

        try
        {
            rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "nosuchstage", "build", "reports", "");
            fail("Shouldn't work for invalid stage name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("build '1' does not have a stage named 'nosuchstage'"));
        }

        try
        {
            rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "nosuchcommand", "reports", "");
            fail("Shouldn't work for invalid command name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("build '1' stage 'default' does not have a command named 'nosuchcommand'"));
        }

        try
        {
            rpcClient.RemoteApi.getArtifactFileListing(project, buildId, "default", "build", "nosuchartifact", "");
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

        Hashtable<String, Object> status = rpcClient.RemoteApi.waitForBuildRequestToBeHandled(ids.get(0), REQUEST_TIMEOUT);
        assertRequestStatusIn(status, BuildRequestRegistry.RequestStatus.ACTIVATED, BuildRequestRegistry.RequestStatus.QUEUED);
        rpcClient.RemoteApi.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testWaitForBuildRequestToBeActivated() throws Exception
    {
        String projectName = randomName();
        Vector<String> ids = insertAndTriggerProject(projectName);

        Hashtable<String, Object> status = rpcClient.RemoteApi.waitForBuildRequestToBeActivated(ids.get(0), REQUEST_TIMEOUT);
        assertFirstActivatedBuild(status);
        rpcClient.RemoteApi.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testGetBuildRequestStatus() throws Exception
    {
        String projectName = randomName();
        Vector<String> ids = insertAndTriggerProject(projectName);

        String id = ids.get(0);
        rpcClient.RemoteApi.waitForBuildRequestToBeActivated(id, REQUEST_TIMEOUT);
        Hashtable<String, Object> status = rpcClient.RemoteApi.getBuildRequestStatus(id);
        assertFirstActivatedBuild(status);
        rpcClient.RemoteApi.waitForBuildToComplete(projectName, 1, BUILD_TIMEOUT);
    }

    public void testGetBuildRequestStatusPaused() throws Exception
    {
        String projectName = randomName();
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        rpcClient.RemoteApi.doConfigAction(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName), ProjectConfigurationActions.ACTION_PAUSE);

        Vector<String> ids = rpcClient.RemoteApi.triggerBuild(projectName, new Hashtable<String, Object>());
        String id = ids.get(0);
        rpcClient.RemoteApi.waitForBuildRequestToBeHandled(id, REQUEST_TIMEOUT);
        Hashtable<String, Object> status = rpcClient.RemoteApi.getBuildRequestStatus(id);
        assertEquals("REJECTED", status.get("status"));
        assertEquals("project state (paused) does not allow building", status.get("rejectionReason"));
    }

    private Vector<String> insertAndTriggerProject(String projectName) throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(projectName);
        Vector<String> ids = rpcClient.RemoteApi.triggerBuild(projectName, new Hashtable<String, Object>());
        assertEquals(1, ids.size());
        return ids;
    }

    private void assertFirstActivatedBuild(Hashtable<String, Object> status)
    {
        assertRequestStatusIn(status, BuildRequestRegistry.RequestStatus.ACTIVATED);
        assertEquals("1", status.get("buildId"));
    }

    private void assertRequestStatusIn(Hashtable<String, Object> status, BuildRequestRegistry.RequestStatus... allowedStatuses)
    {
        assertThat(CollectionUtils.map(allowedStatuses, new ToStringMapping<BuildRequestRegistry.RequestStatus>()), hasItem((String) status.get("status")));
    }

    private void ensureProjectHierarchy() throws Exception
    {
        if (!rpcClient.RemoteApi.configPathExists(PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PROJECT_HIERARCHY_TEMPLATE)))
        {
            rpcClient.RemoteApi.insertSingleCommandProject(PROJECT_HIERARCHY_TEMPLATE, ProjectManager.GLOBAL_PROJECT_NAME, true, rpcClient.RemoteApi.getSubversionConfig(Constants.TRIVIAL_ANT_REPOSITORY), rpcClient.RemoteApi.getAntConfig());
            rpcClient.RemoteApi.insertProject(PROJECT_HIERARCHY_CHILD1, PROJECT_HIERARCHY_TEMPLATE, false, null, null);
            rpcClient.RemoteApi.insertProject(PROJECT_HIERARCHY_CHILD2, PROJECT_HIERARCHY_TEMPLATE, false, null, null);

            rpcClient.RemoteApi.runBuild(PROJECT_HIERARCHY_CHILD1);
            rpcClient.RemoteApi.runBuild(PROJECT_HIERARCHY_CHILD2);
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
