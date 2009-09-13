package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.commands.core.ExecutableCommandTestBase;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.PersistentFeature;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.List;

public class Maven2CommandTest extends ExecutableCommandTestBase
{
    public void testBasic() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        successRun(command, "[compiler:compile", "BUILD SUCCESSFUL");
    }

    public void testExtractVersion() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        PulseExecutionContext context = new PulseExecutionContext();
        runCommand(command, context);
        assertEquals("1.0-SNAPSHOT", context.getVersion());
    }

    public void testNoTarget() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        failedRun(command, "BUILD FAILURE", "You must specify at least one goal");
    }

    public void testMultiGoal() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile test");
        successRun(command, "BUILD SUCCESSFUL", "Running com.zutubi.maven2.test.AppTest",
                "task-segment: [compile, test]", "[compiler:compile", "[compiler:testCompile", "[surefire:test",
                "Tests run: 1, Failures: 0, Errors: 0");
    }

    public void testNoPOM() throws Exception
    {
        prepareBaseDir("nopom");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun(command, "BUILD ERROR", "Cannot execute mojo: resources", "It requires a project with an existing pom.xml");
    }

    public void testNonDefaultPOM() throws Exception
    {
        prepareBaseDir("nondefaultpom");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        command.addArguments("-f");
        command.addArguments("blah/pom.xml");
        successRun(command, "[compiler:compile", "BUILD SUCCESSFUL");
    }

    public void testCompilerError() throws Exception
    {
        prepareBaseDir("compilererror");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun(command, "Compilation failure", "BUILD FAILURE", "task-segment: [compile]");
    }

    public void testTestFailure() throws Exception
    {
        prepareBaseDir("testfailure");

        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        failedRun(command, "task-segment: [test]", "There are test failures.");
    }

    public void testAppliesProcessor() throws Exception
    {
        prepareBaseDir("testfailure");

        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        CommandResult result = failedRun(command);
        List<PersistentFeature> features = result.getArtifact("command output").getFeatures(Feature.Level.ERROR);
        // unfortunately, different versions of the maven surefire plugin (responsible for running the unit tests)
        // result in different output, significantly to the point where a different number of features are captured.
        // For this reason we assert that we have some features captured, not that we have exactly 2 (plugin version 1.5.3)
        // or 3 (plugin version 2.0).
        assertTrue(features.size() > 0);
        assertOutputContains(features.get(0).getSummary(), "Running com.zutubi.maven2.test.AppTest", "Tests run: 1, Failures: 1, Errors: 0,");
    }

    private void prepareBaseDir(String name) throws IOException
    {
        FileSystemUtils.rmdir(baseDir);
        assertTrue(baseDir.mkdir());

        unzipInput(name, baseDir);
    }

    protected File getCommandOutput(CommandResult commandResult) throws IOException
    {
        File output = getCommandArtifact(commandResult, commandResult.getArtifact(Maven2Command.OUTPUT_ARTIFACT_NAME));

        File cleaned = new File(output.getAbsolutePath() + ".cleaned");

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(output));
            writer = new BufferedWriter(new FileWriter(cleaned));
            String line;
            while ((line = reader.readLine()) != null)
            {
                if (!line.contains("Total time") && !line.contains("Finished at") && !line.contains("Final Memory"))
                {
                    line = line.replace(baseDir.getAbsolutePath(), "base.dir");
                    line = line.replaceAll("elapsed: .* sec", "elapsed: x sec");
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
        finally
        {
            IOUtils.close(reader);
            IOUtils.close(writer);
        }

        return cleaned;
    }

    protected String getBuildFileName()
    {
        // we do not use the available copyBuildFile method in the base class.
        return null;
    }

    protected String getBuildFileExt()
    {
        // we do not use the available copyBuildFile method in the base class.
        return null;
    }
}
