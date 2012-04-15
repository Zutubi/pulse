package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.utils.workspace.SubversionWorkspace;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.master.xwork.actions.project.ProjectHealth;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.EnumUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.io.FileSystemUtils;
import org.tmatesoft.svn.core.SVNException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.zutubi.util.Constants.MINUTE;
import static java.util.Arrays.asList;

/**
 * Tests for {@link com.zutubi.pulse.master.api.MonitorApi} XML-RPC methods.
 */
public class MonitorXmlRpcAcceptanceTest extends AcceptanceTestBase
{
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_PROJECTS = "projects";
    private static final String KEY_PERSONAL = "personal";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_HEALTH = "health";
    private static final String KEY_IN_PROGRESS = "inProgress";
    private static final String KEY_LATEST_COMPLETED = "latestCompleted";
    private static final String KEY_COMPLETED_SINCE = "completedSince";
    private static final String KEY_ID = "id";

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
        rpcClient.loginAsAdmin();
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();
        super.tearDown();
    }

    public void testGetStatusForProjectsNoProjects() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(0), false, "");
        assertSensibleTimestamp(statuses);
        assertProjects(statuses);
        assertFalse(statuses.containsKey(KEY_PERSONAL));
    }

    public void testGetStatusForProjectsNoBuilds() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, "");
        assertSensibleTimestamp(statuses);
        assertProjects(statuses, random);
        assertFalse(statuses.containsKey(KEY_PERSONAL));

        Hashtable<String, Object> status = getProject(statuses, random);
        assertHealth(status, ProjectHealth.UNKNOWN);
        assertInProgress(status);
        assertCompletedSince(status);
        assertLatestCompleted(status, null);
    }

    public void testGetStatusForProjectsBuilds() throws Exception
    {
        File tempDir = createTempDirectory();
        try
        {
            ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
            ConfigurationHelper configurationHelper = factory.create(rpcClient.RemoteApi);
            ProjectConfigurations projects  = new ProjectConfigurations(configurationHelper);

            // Start an initial build, check it shows up as in progress.
            WaitProject project = projects.createWaitAntProject(random, tempDir, true);
            configurationHelper.insertProject(project.getConfig(), false);
            rpcClient.RemoteApi.waitForProjectToInitialise(project.getName());
            rpcClient.RemoteApi.triggerBuild(project.getName());
            rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

            Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, "");
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, random);

            Hashtable<String, Object> status = getProject(statuses, random);
            assertHealth(status, ProjectHealth.UNKNOWN);
            assertInProgress(status, 1);
            assertCompletedSince(status);
            assertLatestCompleted(status, null);

            // Release the build, check it appears as latest completed
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);

            statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, (String) statuses.get(KEY_TIMESTAMP));
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, random);

            status = getProject(statuses, random);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status);
            assertCompletedSince(status);
            assertLatestCompleted(status, 1);

            // Run two complete builds and start another, we should get the
            // full compliment of in progress, completed since and latest
            // completed.
            rpcClient.RemoteApi.triggerBuild(project.getName());
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 2);
            rpcClient.RemoteApi.triggerBuild(project.getName());
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 3);
            rpcClient.RemoteApi.triggerBuild(project.getName());
            rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 4);

            statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, (String) statuses.get(KEY_TIMESTAMP));
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, random);

            status = getProject(statuses, random);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status, 4);
            assertCompletedSince(status, 2);
            assertLatestCompleted(status, 3);

            // Release the remaining build and confirm final status.
            project.releaseBuild();
            rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 4);

            statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, (String) statuses.get(KEY_TIMESTAMP));
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, random);

            status = getProject(statuses, random);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status);
            assertCompletedSince(status);
            assertLatestCompleted(status, 4);

            // Check for multiple completed since builds with a dummy timestamp
            statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(random)), false, "1");
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, random);

            status = getProject(statuses, random);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status);
            assertLatestCompleted(status, 4);
            assertCompletedSince(status, 3, 2, 1);
        }
        finally
        {
            FileSystemUtils.rmdir(tempDir);
        }
    }

    public void testGetStatusForProjectsIncludePersonalNoBuilds() throws Exception
    {
        String login = random + "-user";
        rpcClient.RemoteApi.insertTrivialUser(login);

        rpcClient.logout();
        rpcClient.login(login, "");

        Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(0), true, "");
        assertSensibleTimestamp(statuses);
        assertProjects(statuses);
        assertTrue(statuses.containsKey(KEY_PERSONAL));

        Hashtable<String, Object> status = getPersonal(statuses);
        assertHealth(status, ProjectHealth.UNKNOWN);
        assertInProgress(status);
        assertCompletedSince(status);
        assertLatestCompleted(status, null);
    }

    public void testGetStatusForProjectsIncludePersonalBuilds() throws Exception
    {
        String project = random + "-project";
        String login = random + "-user";
        rpcClient.RemoteApi.insertTrivialUser(login);
        rpcClient.RemoteApi.ensureUserCanRunPersonalBuild(login);
        rpcClient.RemoteApi.insertSimpleProject(project);

        rpcClient.logout();
        rpcClient.login(login, "");

        File workingCopy = createTempDirectory();
        try
        {
            PersonalBuildRunner buildRunner = setupPersonalBuild(login, project, workingCopy);
            buildRunner.triggerAndWaitForBuild();

            Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(project)), true, "");
            assertSensibleTimestamp(statuses);
            assertProjects(statuses, project);
            assertTrue(statuses.containsKey(KEY_PERSONAL));

            Hashtable<String, Object> status = getProject(statuses, project);
            assertInProgress(status);
            assertCompletedSince(status);
            assertLatestCompleted(status, null);

            status = getPersonal(statuses);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status);
            assertCompletedSince(status);
            assertLatestCompleted(status, 1);

            buildRunner.triggerAndWaitForBuild();
            buildRunner.triggerAndWaitForBuild();

            statuses = rpcClient.MonitorApi.getStatusForProjects(new Vector<String>(asList(project)), true, (String) statuses.get(KEY_TIMESTAMP));

            status = getProject(statuses, project);
            assertInProgress(status);
            assertCompletedSince(status);
            assertLatestCompleted(status, null);

            status = getPersonal(statuses);
            assertHealth(status, ProjectHealth.OK);
            assertInProgress(status);
            assertCompletedSince(status, 2);
            assertLatestCompleted(status, 3);
        }
        finally
        {
            FileSystemUtils.rmdir(workingCopy);
        }
    }

    public void testGetStatusForAllProjects() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random);
        
        Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForAllProjects(false, "");
        assertSensibleTimestamp(statuses);
        assertNotNull(getProject(statuses, random));
        assertFalse(statuses.containsKey(KEY_PERSONAL));
    }

    public void testGetStatusForMyProjects() throws Exception
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

        Hashtable<String,Object> statuses = rpcClient.MonitorApi.getStatusForMyProjects(false, "");
        assertSensibleTimestamp(statuses);
        assertProjects(statuses, project);
        assertFalse(statuses.containsKey(KEY_PERSONAL));
    }

    private Hashtable<String, Object> getProject(Hashtable<String, Object> statuses, final String projectName)
    {
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> projects = (Vector<Hashtable<String, Object>>) statuses.get(KEY_PROJECTS);
        return CollectionUtils.find(projects, new Predicate<Hashtable<String, Object>>()
        {
            public boolean satisfied(Hashtable<String, Object> project)
            {
                return projectName.equals(project.get(KEY_OWNER));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Hashtable<String, Object> getPersonal(Hashtable<String, Object> statuses)
    {
        return (Hashtable<String, Object>) statuses.get(KEY_PERSONAL);
    }

    private void assertSensibleTimestamp(Hashtable<String, Object> statuses)
    {
        assertTrue(statuses.containsKey(KEY_TIMESTAMP));
        long timestamp = Long.parseLong((String) statuses.get(KEY_TIMESTAMP));
        long timeNow = System.currentTimeMillis();
        assertTrue(timestamp + 5 * MINUTE > timeNow);
        assertTrue(timestamp <= timeNow);
    }

    private void assertProjects(Hashtable<String, Object> statuses, String... expectedProjects)
    {
        assertTrue(statuses.containsKey(KEY_PROJECTS));
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> projects = (Vector<Hashtable<String, Object>>) statuses.get(KEY_PROJECTS);
        Set<String> gotProjects = CollectionUtils.map(projects, new Mapping<Hashtable<String, Object>, String>()
        {
            public String map(Hashtable<String, Object> status)
            {
                return (String) status.get(KEY_OWNER);
            }
        }, new HashSet<String>());

        assertEquals(new HashSet<String>(asList(expectedProjects)), gotProjects);
    }

    private void assertHealth(Hashtable<String, Object> status, ProjectHealth health)
    {
        assertEquals(EnumUtils.toPrettyString(health), status.get(KEY_HEALTH));
    }

    private void assertInProgress(Hashtable<String, Object> status, Integer... expectedIds)
    {
        assertBuilds(status, KEY_IN_PROGRESS, expectedIds);
    }

    private void assertCompletedSince(Hashtable<String, Object> status, Integer... expectedIds)
    {
        assertBuilds(status, KEY_COMPLETED_SINCE, expectedIds);
    }

    private void assertLatestCompleted(Hashtable<String, Object> status, Integer expectedId)
    {
        if (expectedId == null)
        {
            assertFalse(status.containsKey(KEY_LATEST_COMPLETED));
        }
        else
        {
            assertTrue(status.containsKey(KEY_LATEST_COMPLETED));
            @SuppressWarnings("unchecked")
            Hashtable<String, Object> latest = (Hashtable<String, Object>) status.get(KEY_LATEST_COMPLETED);
            assertEquals(expectedId, latest.get("id"));
        }
    }

    private void assertBuilds(Hashtable<String, Object> status, String key, Integer... expectedIds)
    {
        assertTrue(status.containsKey(key));
        @SuppressWarnings("unchecked")
        Vector<Hashtable<String, Object>> builds = (Vector<Hashtable<String, Object>>) status.get(key);
        List<Integer> gotBuilds = CollectionUtils.map(builds, new Mapping<Hashtable<String, Object>, Integer>()
        {
            public Integer map(Hashtable<String, Object> build)
            {
                return (Integer) build.get(KEY_ID);
            }
        });

        assertEquals(asList(expectedIds), gotBuilds);
    }

    private PersonalBuildRunner setupPersonalBuild(String user, String project, File workingCopy) throws SVNException, IOException, ClientException
    {
        SubversionWorkspace workspace = new SubversionWorkspace(workingCopy, "pulse", "pulse");
        workspace.doCheckout(Constants.TRIVIAL_ANT_REPOSITORY);
        File newFile = new File(workingCopy, "file.txt");
        FileSystemUtils.createFile(newFile, "new file");
        workspace.doAdd(newFile);

        PersonalBuildRunner buildRunner = new PersonalBuildRunner(rpcClient.RemoteApi);
        buildRunner.setBase(workingCopy);
        buildRunner.createConfigFile(baseUrl, user, "", project);
        return buildRunner;
    }
}
