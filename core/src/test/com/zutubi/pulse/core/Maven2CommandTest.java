package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.BuildContext;

import java.io.*;
import java.util.List;

/**
 * <class-comment/>
 */
public class Maven2CommandTest extends PulseTestCase
{
    File baseDir;
    File outputDir;

    public void setUp() throws IOException
    {
        baseDir = FileSystemUtils.createTempDir(getClass().getName(), ".base");
        outputDir = FileSystemUtils.createTempDir(getClass().getName(), ".out");
    }

    public void tearDown() throws IOException
    {
        removeDirectory(baseDir);
        removeDirectory(outputDir);
    }

    public void testBasic() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        successRun("basic", command, "[compiler:compile]", "BUILD SUCCESSFUL");
    }

    public void testExtractVersion() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        BuildContext buildContext = new BuildContext();
        runMaven("basic", command, buildContext);
        assertEquals("1.0-SNAPSHOT", buildContext.getBuildVersion());
    }

    public void testNoTarget() throws Exception
    {
        Maven2Command command = new Maven2Command();
        failedRun("basic", command, "BUILD FAILURE", "You must specify at least one goal");
    }

    public void testMultiGoal() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile test");
        successRun("basic", command, "BUILD SUCCESSFUL", "Running com.zutubi.maven2.test.AppTest",
                "task-segment: [compile, test]", "[compiler:compile]", "[compiler:testCompile]", "[surefire:test]",
                "Tests run: 1, Failures: 0, Errors: 0");
    }

    public void testNoPOM() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun("nopom", command, "BUILD ERROR", "Cannot execute mojo: resources", "It requires a project with an existing pom.xml");
    }

    public void testCompilerError() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun("compilererror", command, "Compilation failure", "BUILD FAILURE", "task-segment: [compile]");
    }

    public void testTestFailure() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        failedRun("testfailure", command, "task-segment: [test]", "There are test failures.");
    }

    public void testAppliesProcessor() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        CommandResult result = failedRun("testfailure", command);
        List<Feature> features = result.getArtifact("command output").getFeatures(Feature.Level.ERROR);
        // unfortunately, different versions of the maven surefire plugin (responsible for running the unit tests)
        // result in different output, significantly to the point where a different number of features are captured.
        // For this reason we assert that we have some features captured, not that we have exactly 2 (plugin version 1.5.3)
        // or 3 (plugin version 2.0).
        assertTrue(features.size() > 0);
        assertOutputContains(features.get(0).getSummary(), "Running com.zutubi.maven2.test.AppTest", "Tests run: 1, Failures: 1, Errors: 0,");
    }

    private CommandResult successRun(String inName, Maven2Command command, String ...contents) throws Exception
    {
        CommandResult result = runMaven(inName, command, null);
        assertTrue(result.succeeded());
        checkOutput(contents);
        return result;
    }

    private CommandResult failedRun(String inName, Maven2Command command, String ...contents) throws Exception
    {
        CommandResult result = runMaven(inName, command, null);
        assertTrue(result.failed());
        checkOutput(contents);
        return result;
    }

    private CommandResult runMaven(String inName, Maven2Command command, BuildContext buildContext) throws Exception
    {
        File sourceDir = getSource(inName);
        FileSystemUtils.rmdir(baseDir);
        FileSystemUtils.copy(baseDir, sourceDir);

        // Remove the .in extension from all files
        removeInExtension(baseDir);

        CommandResult result = new CommandResult("maven2-test");
        CommandContext context = new CommandContext(new SimpleRecipePaths(baseDir, null), outputDir, null);
        context.setBuildContext(buildContext);
        command.execute(context, result);

        return result;
    }

    private void removeInExtension(File f)
    {
        if(f.isDirectory())
        {
            for(String name: f.list())
            {
                removeInExtension(new File(f, name));
            }
        }
        else
        {
            if(f.getName().endsWith(".in"))
            {
                String path = f.getAbsolutePath();
                FileSystemUtils.rename(f, new File(path.substring(0, path.length() - 3)));
            }
        }
    }

    private File getSource(String name)
    {
        return new File(getPulseRoot(), FileSystemUtils.composeFilename("core", "src", "test", "com", "zutubi", "pulse", "core", getClass().getSimpleName() + "." + name));
    }

    protected void checkOutput(String ...contents) throws IOException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(cleanOutput());
            String output = IOUtils.inputStreamToString(is);
            assertOutputContains(output, contents);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    private void assertOutputContains(String output, String... contents)
    {
        for (String content : contents)
        {
            if (!output.contains(content))
            {
                fail("Output '" + output + "' does not contain '" + content + "'");
            }
        }
    }

    private File cleanOutput() throws IOException
    {
        File outputArtifactDir = new File(outputDir, "command output");
        File outputArtifact = new File(outputArtifactDir, "output.txt");
        File cleaned = new File(outputArtifact.getAbsolutePath() + ".cleaned");

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(outputArtifact));
            writer = new BufferedWriter(new FileWriter(cleaned));
            String line;
            while((line = reader.readLine()) != null)
            {
                if(!line.contains("Total time") && !line.contains("Finished at") && !line.contains("Final Memory"))
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

}
