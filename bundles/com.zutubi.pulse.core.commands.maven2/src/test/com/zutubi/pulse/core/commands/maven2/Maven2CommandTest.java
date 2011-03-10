package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.api.TestCommandContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestCase;
import com.zutubi.pulse.core.engine.api.FieldScope;
import com.zutubi.pulse.core.engine.api.ResultState;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.SystemUtils;

import java.io.IOException;

public class Maven2CommandTest extends ExecutableCommandTestCase
{
    public void testBasic() throws Exception
    {
        prepareBaseDir("basic");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        successRun(command, "[compiler:compile", "BUILD SUCCESSFUL");
    }

    public void testExtractVersion() throws Exception
    {
        prepareBaseDir("basic");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        TestCommandContext context = runCommand(new Maven2Command(command), createExecutionContext());
        assertEquals("1.0-SNAPSHOT", context.getCustomFields().get(asPair(FieldScope.BUILD, "maven.version")));
    }

    public void testNoTarget() throws Exception
    {
        prepareBaseDir("basic");

        Maven2CommandConfiguration command = createCommandConfig();
        failedRun(command, "BUILD FAILURE", "You must specify at least one goal");
    }

    public void testMultiGoal() throws Exception
    {
        prepareBaseDir("basic");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile test");
        successRun(command, "BUILD SUCCESSFUL", "Running com.zutubi.maven2.test.AppTest",
                "task-segment: [compile, test]", "[compiler:compile", "[compiler:testCompile", "[surefire:test",
                "Tests run: 1, Failures: 0, Errors: 0");
    }

    public void testNoPOM() throws Exception
    {
        prepareBaseDir("nopom");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        failedRun(command, "BUILD ERROR", "Cannot execute mojo: resources", "It requires a project with an existing pom.xml");
    }

    public void testNonDefaultPOM() throws Exception
    {
        prepareBaseDir("nondefaultpom");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        command.setPomFile("blah/pom.xml");
        successRun(command, "[compiler:compile", "BUILD SUCCESSFUL");
    }

    public void testCompilerError() throws Exception
    {
        prepareBaseDir("compilererror");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("compile");
        failedRun(command, "Compilation failure", "BUILD FAILURE", "task-segment: [compile]");
    }

    private Maven2CommandConfiguration createCommandConfig()
    {
        Maven2CommandConfiguration config = new Maven2CommandConfiguration();
        String m2Home = System.getenv("MAVEN2_HOME");
        assertNotNull("MAVEN2_HOME must be set to the path of the maven 2 installation", m2Home);
        config.setExe(m2Home + "/bin/mvn" + (SystemUtils.IS_WINDOWS ? ".bat" : ""));
        return config;
    }

    public void testTestFailure() throws Exception
    {
        prepareBaseDir("testfailure");

        Maven2CommandConfiguration command = createCommandConfig();
        command.setGoals("test");
        failedRun(command, "task-segment: [test]", "There are test failures.");
    }

    private void prepareBaseDir(String name) throws IOException
    {
        FileSystemUtils.rmdir(baseDir);
        assertTrue(baseDir.mkdir());

        unzipInput(name, baseDir);
    }

    private TestCommandContext successRun(Maven2CommandConfiguration configuration, String... content) throws Exception
    {
        return successRun(new Maven2Command(configuration), content);
    }

    private TestCommandContext failedRun(Maven2CommandConfiguration configuration, String... content) throws Exception
    {
        TestCommandContext context = runCommand(new Maven2Command(configuration));
        if (!SystemUtils.IS_WINDOWS)
        {
            assertEquals(ResultState.FAILURE, context.getResultState());
        }
        assertDefaultOutputContains(content);
        return context;
    }
}
