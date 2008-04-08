package com.zutubi.pulse.acceptance;

import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.prototype.config.project.BuildSelectorConfiguration;
import com.zutubi.pulse.prototype.config.project.hooks.*;
import com.zutubi.pulse.test.TestUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * Tests for build hooks, both configuration and ensuring they are executed
 * when expected.
 */
public class BuildHookAcceptanceTest extends SeleniumTestBase
{
    private static final String PROJECT_NAME = "hook-test-project";
    private static final String PROJECT_PATH = PathUtils.getPath(ConfigurationRegistry.PROJECTS_SCOPE, "hook-test-project");
    private static final String HOOKS_PATH   = PathUtils.getPath(PROJECT_PATH, "buildHooks");

    private static final File DUMPENV_JAR = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("acceptance", "src", "test", "misc", "dumpenv.jar"));

    private File tempDir;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(BuildHookAcceptanceTest.class.getName(), "");
        xmlRpcHelper.loginAsAdmin();
        xmlRpcHelper.ensureProject(PROJECT_NAME);
        xmlRpcHelper.deleteAllConfigs(PathUtils.getPath(HOOKS_PATH, PathUtils.WILDCARD_ANY_ELEMENT));
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();
        xmlRpcHelper.logout();
        FileSystemUtils.rmdir(tempDir);
        super.tearDown();
    }

    public void testPreBuildHook() throws Exception
    {
        chooseHookType("zutubi.preBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PreBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "false");

        addTask("${project} ${status}");

        xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        assertArgs(PROJECT_NAME, "${status}");
    }

    public void testPostBuildHook() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "true", null, "false");

        selectFromAllTasks();
        addTask("${project} ${status}");

        xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        assertArgs(PROJECT_NAME, "success");
    }

    public void testPostStageHook() throws Exception
    {
        chooseHookType("zutubi.postStageHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostStageHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "true", null, "true", null, "false");

        addTask("${project} ${stage} ${recipe} ${status}");

        xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        assertArgs(PROJECT_NAME, "default", "[default]", "success");
    }

    public void testManualHook() throws Exception
    {
        chooseHookType("zutubi.manualBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, ManualBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random);

        selectFromAllTasks();

        CompositePage hookPage = addTask("${build.number} ${project} ${status}");
        int buildNumber = xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        triggerHook(hookPage, buildNumber);
        assertArgs(Long.toString(buildNumber), PROJECT_NAME, "success");
    }

    public void testManuallyTriggerPreBuildHook() throws Exception
    {
        int buildNumber = xmlRpcHelper.runBuild(PROJECT_NAME, 60000);

        chooseHookType("zutubi.preBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PreBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "false");

        CompositePage hookPage = addTask("${project} ${status}");
        triggerHook(hookPage, buildNumber);
        assertArgs(PROJECT_NAME, "success");
    }

    public void testManuallyTriggerPostStageHook() throws Exception
    {
        int buildNumber = xmlRpcHelper.runBuild(PROJECT_NAME, 60000);

        chooseHookType("zutubi.postStageHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostStageHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "true", null, "true", null, "false");

        CompositePage hookPage = addTask("${project} ${stage} ${recipe} ${status}");
        triggerHook(hookPage, buildNumber);
        assertArgs(PROJECT_NAME, "default", "[default]", "success");
    }

    public void testFailOnError() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "true", null, "true");

        selectFromAllTasks();
        ConfigurationForm taskForm = new ConfigurationForm(selenium, RunExecutableTaskConfiguration.class);
        taskForm.waitFor();
        taskForm.finishFormElements("nosuchexe", null, tempDir.getAbsolutePath(), null, null);

        waitForHook();
        int buildNumber = xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        Hashtable<String,Object> build = xmlRpcHelper.getBuild(PROJECT_NAME, buildNumber);
        assertEquals("error", build.get("status"));
    }

    public void testRestrictToStates() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "false", "error", "false");

        selectFromAllTasks();
        addTask("${project}");

        xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        assertFalse(new File(tempDir, "args.txt").exists());
    }

    public void testDisable() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random, "true", null, "false");

        selectFromAllTasks();
        CompositePage hookPage = addTask("${project} ${status}");
        hookPage.clickActionAndWait("disable");
        assertEquals("disabled", hookPage.getStateField("state"));

        xmlRpcHelper.runBuild(PROJECT_NAME, 60000);
        assertFalse(new File(tempDir, "args.txt").exists());
    }

    public void testTriggerFromBuildPage() throws Exception
    {
        chooseHookType("zutubi.manualBuildHookConfig");

        ConfigurationForm hookForm = new ConfigurationForm(selenium, ManualBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextFormElements(random);

        selectFromAllTasks();

        addTask("${build.number} ${project} ${status}");
        int buildNumber = xmlRpcHelper.runBuild(PROJECT_NAME, 60000);

        BuildSummaryPage summaryPage = new BuildSummaryPage(selenium, urls, PROJECT_NAME, buildNumber);
        summaryPage.goTo();
        summaryPage.clickHook(random);
        SeleniumUtils.waitForVisible(selenium, "status-message");
        assertTextPresent("triggered hook '" + random + "'");

        waitForTask();
        assertArgs(Long.toString(buildNumber), PROJECT_NAME, "success");
    }

    private void triggerHook(CompositePage hookPage, int buildNumber) throws InterruptedException
    {
        File envFile = new File(tempDir, "env.txt");
        assertFalse(envFile.exists());

        hookPage.goTo();
        hookPage.clickAction("trigger");
        ConfigurationForm buildForm = new ConfigurationForm(selenium, BuildSelectorConfiguration.class);
        buildForm.waitFor();
        String number = Long.toString(buildNumber);
        buildForm.saveFormElements(number);

        waitForTask();
    }

    private void waitForTask() throws InterruptedException
    {
        File envFile = new File(tempDir, "env.txt");
        long startTime = System.currentTimeMillis();
        while(!envFile.exists())
        {
            assertTrue(System.currentTimeMillis() - startTime < 60000);
            Thread.sleep(500);
        }
    }

    private SelectTypeState chooseHookType(String symbolicName)
    {
        ListPage hooksPage = new ListPage(selenium, urls, HOOKS_PATH);
        hooksPage.goTo();
        hooksPage.clickAdd();

        SelectTypeState hookType = new SelectTypeState(selenium);
        hookType.waitFor();
        assertEquals(Arrays.asList("zutubi.manualBuildHookConfig", "zutubi.postBuildHookConfig", "zutubi.postStageHookConfig", "zutubi.preBuildHookConfig"), hookType.getSortedOptionList());
        hookType.nextFormElements(symbolicName);
        return hookType;
    }

    private void selectFromAllTasks()
    {
        SelectTypeState taskType = new SelectTypeState(selenium);
        taskType.waitFor();
        assertEquals(Arrays.asList("zutubi.emailCommittersTaskConfig", "zutubi.runExecutableTaskConfig", "zutubi.tagTaskConfig"), taskType.getSortedOptionList());
        taskType.nextFormElements("zutubi.runExecutableTaskConfig");
    }

    private CompositePage addTask(String arguments)
    {
        ConfigurationForm taskForm = new ConfigurationForm(selenium, RunExecutableTaskConfiguration.class);
        taskForm.waitFor();
        taskForm.finishFormElements("java", "-jar " + DUMPENV_JAR.getAbsolutePath().replace('\\', '/') + " " + arguments, tempDir.getAbsolutePath(), null, null);
        return waitForHook();
    }

    private CompositePage waitForHook()
    {
        CompositePage hookPage = new CompositePage(selenium, urls, PathUtils.getPath(HOOKS_PATH, random));
        hookPage.waitFor();
        return hookPage;
    }

    private void assertArgs(String... expected) throws IOException
    {
        File argFile = new File(tempDir, "args.txt");
        String args = IOUtils.fileToString(argFile);
        assertEquals(Arrays.asList(expected), Arrays.asList(args.split("\\r?\\n")));
    }
}
