package com.zutubi.pulse.acceptance;

import com.google.common.io.Files;
import com.zutubi.pulse.acceptance.forms.ConfigurationForm;
import com.zutubi.pulse.acceptance.forms.admin.SelectTypeState;
import com.zutubi.pulse.acceptance.pages.admin.CompositePage;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.browse.AbstractLogPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildLogPage;
import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.browse.StageLogPage;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.build.log.BuildLogFile;
import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.pulse.master.build.log.RecipeLogFile;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.BuildSelectorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationWizard;
import com.zutubi.pulse.master.tove.config.project.hooks.*;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Condition;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.getPath;
import static com.zutubi.util.CollectionUtils.asPair;

/**
 * Tests for build hooks, both configuration and ensuring they are executed
 * when expected.
 */
public class BuildHookAcceptanceTest extends AcceptanceTestBase
{
    private static final int TASK_TIMEOUT = 30000;

    private static final String PROJECT_NAME = "hook-test-project";
    private static final String PROJECT_PATH = getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, "hook-test-project");
    private static final String HOOKS_BASENAME = "buildHooks";
    private static final String HOOKS_PATH   = getPath(PROJECT_PATH, HOOKS_BASENAME);

    private static final String HELLO_MESSAGE = "Hello, hook output.";

    private File tempDir;
    private File dumpEnv;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(BuildHookAcceptanceTest.class.getName(), "");
        dumpEnv = copyInputToDirectory("dumpenv", "jar", tempDir);

        rpcClient.loginAsAdmin();
        rpcClient.RemoteApi.ensureProject(PROJECT_NAME);
        rpcClient.RemoteApi.deleteAllConfigs(getPath(HOOKS_PATH, PathUtils.WILDCARD_ANY_ELEMENT));
        getBrowser().loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testPreBuildHook() throws Exception
    {
        chooseHookType("zutubi.preBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PreBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        addDumpEnvTask("${project} ${status}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        assertArgs(PROJECT_NAME, "${status}");
    }

    public void testPostBuildHook() throws Exception
    {
        postBuildHelper();
        addDumpEnvTask("${project} ${status}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        assertArgs(PROJECT_NAME, "success");
    }

    public void testBuildDirProperty() throws Exception
    {
        postBuildHelper();
        addDumpEnvTask("${build.dir}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        List<String> args = getArgs();

        // The build directory contains the build log.
        LogFile log = new LogFile(new File(args.get(0), BuildLogFile.LOG_FILENAME), false);
        assertTrue(log.exists());
    }

    public void testPostBuildHookCanAccessProjectAndStageProperties() throws Exception
    {
        String projectPath = rpcClient.RemoteApi.insertSimpleProject(random, false);
        rpcClient.RemoteApi.insertProjectProperty(random, "some.property", "some.value");
        String stagePropertiesPath = getPath(projectPath, "stages", "default", "properties");
        Hashtable<String, Object> property = rpcClient.RemoteApi.createProperty("a.stage.property", "a.stage.value");
        rpcClient.RemoteApi.insertConfig(stagePropertiesPath, property);

        chooseHookType(random, "zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromAllTasks();
        addDumpEnvTask(random, "${project} ${some.property} ${stage.default.a.stage.property}");

        rpcClient.RemoteApi.runBuild(random);
        assertArgs(random, "some.value", "a.stage.value");
    }

    public void testPostBuildHookCanAccessTriggerProperty() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleProject(random, false);
        // Also add the property to the project - we want to make sure it is
        // overridden by the value passed on trigger.
        rpcClient.RemoteApi.insertProjectProperty(random, "some.property", "some.value");

        chooseHookType(random, "zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromAllTasks();
        addDumpEnvTask(random, "${some.property}");

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("some.property", "trigger.value");
        int number = rpcClient.RemoteApi.getNextBuildNumber(random);
        rpcClient.RemoteApi.triggerBuild(random, "", properties);
        rpcClient.RemoteApi.waitForBuildToComplete(random, number);
        assertArgs("trigger.value");
    }

    public void testPreStageHook() throws Exception
    {
        preStageHelper();
        addDumpEnvTask("${project} ${stage} ${recipe} ${status}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        assertArgs(PROJECT_NAME, "default", "[default]", "${status}");
    }

    public void testPostStageHook() throws Exception
    {
        postStageHelper();
        addDumpEnvTask("${project} ${stage} ${recipe} ${status}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        assertArgs(PROJECT_NAME, "default", "[default]", "success");
    }

    public void testPostStageHookOnAgent() throws Exception
    {
        rpcClient.RemoteApi.ensureAgent(AGENT_NAME);
        assignStageToAgent(PROJECT_NAME, "default", AGENT_NAME);
        chooseHookType("zutubi.postStageHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostStageHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random), asPair("runTaskOnAgents", "true"));

        selectFromStageTasks();

        addDumpEnvTask("${project} ${stage} ${agent}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        assertArgs(PROJECT_NAME, "default", AGENT_NAME);
    }

    public void testStageDirProperty() throws Exception
    {
        postStageHelper();
        addDumpEnvTask("${stage.dir}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        List<String> args = getArgs();

        // The stage directory contains the recipe log.
        LogFile log = new LogFile(new File(args.get(0), RecipeLogFile.LOG_FILENAME), false);
        assertTrue(log.exists());
    }

    public void testManualHook() throws Exception
    {
        manualHookHelper();

        CompositePage hookPage = addDumpEnvTask("${build.number} ${project} ${status}");
        int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        triggerHookAndWait(hookPage, buildNumber);
        assertArgs(Long.toString(buildNumber), PROJECT_NAME, "success");
    }

    public void testManuallyTriggerPreBuildHook() throws Exception
    {
        int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        chooseHookType("zutubi.preBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PreBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        CompositePage hookPage = addDumpEnvTask("${project} ${status}");
        triggerHookAndWait(hookPage, buildNumber);
        assertArgs(PROJECT_NAME, "success");
    }

    public void testManualTriggerNotAvailableWhenDisabled() throws Exception
    {
        long buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        chooseHookType("zutubi.preBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PreBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random), asPair("allowManualTrigger", "false"));

        CompositePage hookPage = addDumpEnvTask("${project} ${status}");
        assertFalse(hookPage.isActionPresent(BuildHookConfigurationActions.ACTION_TRIGGER));

        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, PROJECT_NAME, buildNumber);
        assertFalse(summaryPage.isHookPresent(random));
    }

    public void testManuallyTriggerPostStageHook() throws Exception
    {
        int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        postStageHelper();
        CompositePage hookPage = addDumpEnvTask("${project} ${stage} ${recipe} ${status}");
        triggerHookAndWait(hookPage, buildNumber);
        assertArgs(PROJECT_NAME, "default", "[default]", "success");
    }

    public void testFailOnError() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random), asPair("failOnError", "true"));

        selectFromAllTasks();
        ConfigurationForm taskForm = getBrowser().createForm(ConfigurationForm.class, RunExecutableTaskConfiguration.class);
        taskForm.waitFor();
        taskForm.finishFormElements("nosuchexe", null, tempDir.getAbsolutePath(), null, null);

        waitForHook(PROJECT_NAME);
        final int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    return rpcClient.RemoteApi.getBuildStatus(PROJECT_NAME, buildNumber) == ResultState.ERROR;
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }, TASK_TIMEOUT, "build to change to error state");
    }

    public void testRestrictToStates() throws Exception
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random), asPair("runForAll", "false"), asPair("runForStates", "error"));

        selectFromAllTasks();
        addDumpEnvTask("${project}");

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        // Give the hooks a little time to run.
        Thread.sleep(2000);
        assertFalse(new File(tempDir, "args.txt").exists());
    }

    public void testDisable() throws Exception
    {
        postBuildHelper();
        CompositePage hookPage = addDumpEnvTask("${project} ${status}");
        hookPage.clickActionAndWait("disable");
        assertEquals("disabled", hookPage.getStateField("state"));

        rpcClient.RemoteApi.runBuild(PROJECT_NAME);
        // Give the hooks a little time to run.
        Thread.sleep(2000);
        assertFalse(new File(tempDir, "args.txt").exists());
    }

    public void testTriggerFromBuildPage() throws Exception
    {
        long buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        manualHookHelper();
        addDumpEnvTask("${build.number} ${project} ${status}");

        BuildSummaryPage summaryPage = getBrowser().openAndWaitFor(BuildSummaryPage.class, PROJECT_NAME, buildNumber);
        summaryPage.clickHook(random);
        getBrowser().waitForVisible("status-message");
        getBrowser().waitForTextPresent("triggered hook '" + random + "'");

        waitForTask();
        assertArgs(Long.toString(buildNumber), PROJECT_NAME, "success");
    }

    public void testManualTriggerOutput() throws Exception
    {
        int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        manualHookHelper();
        File hello = copyInputToDirectory("hello", "jar", tempDir);
        CompositePage hookPage = addJavaTask(PROJECT_NAME, "-jar " + hello.getAbsolutePath().replace('\\', '/'));
        triggerHook(hookPage, buildNumber);

        waitForHookOutput(getBrowser().openAndWaitFor(BuildLogPage.class, PROJECT_NAME, (long) buildNumber));
    }

    public void testManualTriggerPostStageOutput() throws Exception
    {
        int buildNumber = rpcClient.RemoteApi.runBuild(PROJECT_NAME);

        postStageHelper();
        File hello = copyInputToDirectory("hello", "jar", tempDir);
        CompositePage hookPage = addJavaTask(PROJECT_NAME, "-jar " + hello.getAbsolutePath().replace('\\', '/'));
        triggerHook(hookPage, buildNumber);
        
        waitForHookOutput(getBrowser().openAndWaitFor(StageLogPage.class, PROJECT_NAME, (long) buildNumber, ProjectConfigurationWizard.DEFAULT_STAGE));
    }

    private void waitForHookOutput(AbstractLogPage logPage)
    {
        logPage.clickDownloadLink();
        getBrowser().refreshUntil(TASK_TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                return getBrowser().isTextPresent(HELLO_MESSAGE);
            }
        }, "hook output to appear in log");
    }

    private void postBuildHelper()
    {
        chooseHookType("zutubi.postBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromAllTasks();
    }

    private void preStageHelper()
    {
        chooseHookType("zutubi.preStageHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PreStageHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromStageTasks();
    }

    private void postStageHelper()
    {
        chooseHookType("zutubi.postStageHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, PostStageHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromStageTasks();
    }

    private void manualHookHelper()
    {
        chooseHookType("zutubi.manualBuildHookConfig");

        ConfigurationForm hookForm = getBrowser().createForm(ConfigurationForm.class, ManualBuildHookConfiguration.class);
        hookForm.waitFor();
        hookForm.nextNamedFormElements(asPair("name", random));

        selectFromAllTasks();
    }

    private void triggerHookAndWait(CompositePage hookPage, int buildNumber) throws InterruptedException
    {
        triggerHook(hookPage, buildNumber);
        waitForTask();
    }

    private void triggerHook(CompositePage hookPage, int buildNumber)
    {
        File envFile = new File(tempDir, "env.txt");
        assertFalse(envFile.exists());

        hookPage.openAndWaitFor();
        hookPage.clickAction("trigger");
        ConfigurationForm buildForm = getBrowser().createForm(ConfigurationForm.class, BuildSelectorConfiguration.class);
        buildForm.waitFor();
        String number = Long.toString(buildNumber);
        buildForm.saveFormElements(number);
    }

    private void waitForTask() throws InterruptedException
    {
        File envFile = new File(tempDir, "env.txt");
        long startTime = System.currentTimeMillis();
        while(!envFile.exists())
        {
            assertTrue(System.currentTimeMillis() - startTime < TASK_TIMEOUT);
            Thread.sleep(500);
        }
    }

    private SelectTypeState chooseHookType(String symbolicName)
    {
        return chooseHookType(PROJECT_NAME, symbolicName);
    }

    private SelectTypeState chooseHookType(String projectName, String symbolicName)
    {
        ListPage hooksPage = getBrowser().openAndWaitFor(ListPage.class, getHooksPath(projectName));
        hooksPage.clickAdd();

        SelectTypeState hookType = new SelectTypeState(getBrowser());
        hookType.waitFor();
        assertEquals(Arrays.asList("zutubi.manualBuildHookConfig", "zutubi.postBuildHookConfig", "zutubi.postStageHookConfig", "zutubi.preBuildHookConfig", "zutubi.preStageHookConfig", "zutubi.terminateStageHookConfig"), hookType.getSortedOptionList());
        hookType.nextFormElements(symbolicName);
        return hookType;
    }

    private String getHooksPath(String projectName)
    {
        return getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, projectName, HOOKS_BASENAME);
    }

    private void selectFromAllTasks()
    {
        SelectTypeState taskType = new SelectTypeState(getBrowser());
        taskType.waitFor();
        assertEquals(Arrays.asList("zutubi.runExecutableTaskConfig", "zutubi.sendEmailTaskConfig", "zutubi.tagTaskConfig"), taskType.getSortedOptionList());
        taskType.nextFormElements("zutubi.runExecutableTaskConfig");
    }

    private void selectFromStageTasks()
    {
        SelectTypeState taskType = new SelectTypeState(getBrowser());
        taskType.waitFor();
        assertEquals(Arrays.asList("zutubi.runExecutableTaskConfig", "zutubi.tagTaskConfig"), taskType.getSortedOptionList());
        taskType.nextFormElements("zutubi.runExecutableTaskConfig");
    }

    private CompositePage addDumpEnvTask(String arguments)
    {
        return addDumpEnvTask(PROJECT_NAME, arguments);
    }

    private CompositePage addDumpEnvTask(String projectName, String arguments)
    {
        return addJavaTask(projectName, "-jar \"" + dumpEnv.getAbsolutePath().replace('\\', '/') + "\" " + arguments);
    }

    private CompositePage addJavaTask(String projectName, String arguments)
    {
        ConfigurationForm taskForm = getBrowser().createForm(ConfigurationForm.class, RunExecutableTaskConfiguration.class);
        taskForm.waitFor();
        taskForm.finishFormElements("java", arguments, tempDir.getAbsolutePath(), null, null);
        return waitForHook(projectName);
    }

    private CompositePage waitForHook(String projectName)
    {
        return getBrowser().openAndWaitFor(CompositePage.class, getPath(getHooksPath(projectName), random));
    }

    private List<String> getArgs() throws IOException
    {
        File argFile = new File(tempDir, "args.txt");
        TestUtils.waitForCondition(new FileExistsCondition(argFile), TASK_TIMEOUT, "file '" + argFile.getAbsolutePath() + "' to exist");
        String args = Files.toString(argFile, Charset.defaultCharset());
        return Arrays.asList(args.split("\\r?\\n"));
    }

    private void assertArgs(String... expected) throws IOException
    {
        List<String> gotArgs = getArgs();
        assertEquals(Arrays.asList(expected), gotArgs);
    }
}
