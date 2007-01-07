package com.zutubi.pulse.core;

import com.zutubi.pulse.BuildContext;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * <class-comment/>
 */
public class Maven2CommandTest extends CommandTestBase
{
    public void testBasic() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        successRun(command, "[compiler:compile]", "BUILD SUCCESSFUL");
    }

    public void testExtractVersion() throws Exception
    {
        prepareBaseDir("basic");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        BuildContext buildContext = new BuildContext();
        runCommand(command, buildContext);
        assertEquals("1.0-SNAPSHOT", buildContext.getBuildVersion());
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
                "task-segment: [compile, test]", "[compiler:compile]", "[compiler:testCompile]", "[surefire:test]",
                "Tests run: 1, Failures: 0, Errors: 0");
    }

    public void testNoPOM() throws Exception
    {
        prepareBaseDir("nopom");

        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun(command, "BUILD ERROR", "Cannot execute mojo: resources", "It requires a project with an existing pom.xml");
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
        List<Feature> features = result.getArtifact("command output").getFeatures(Feature.Level.ERROR);
        // unfortunately, different versions of the maven surefire plugin (responsible for running the unit tests)
        // result in different output, significantly to the point where a different number of features are captured.
        // For this reason we assert that we have some features captured, not that we have exactly 2 (plugin version 1.5.3)
        // or 3 (plugin version 2.0).
        assertTrue(features.size() > 0);
        assertOutputContains(features.get(0).getSummary(), "Running com.zutubi.maven2.test.AppTest", "Tests run: 1, Failures: 1, Errors: 0,");
    }

    private void prepareBaseDir(String name) throws IOException
    {
        try
        {
            File sourceDir = getSource(name);
            FileSystemUtils.rmdir(baseDir);
            FileSystemUtils.copy(baseDir, sourceDir);

            // Remove the .in extension from all files
            removeInExtension(baseDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void removeInExtension(File f)
    {
        if (f.isDirectory())
        {
            for (String name : f.list())
            {
                removeInExtension(new File(f, name));
            }
        }
        else
        {
            if (f.getName().endsWith(".in"))
            {
                String path = f.getAbsolutePath();
                FileSystemUtils.rename(f, new File(path.substring(0, path.length() - 3)));
            }
        }
    }

    private File getSource(String name)
    {
        URL resource = getClass().getResource("Maven2CommandLoadTest.basic.xml");
        File moduleDir = new File(resource.getPath().replaceFirst("command-maven2.*", "command-maven2"));

        return new File(moduleDir, FileSystemUtils.composeFilename("src", "test", "com", "zutubi", "pulse", "core", getClass().getSimpleName() + "." + name));
    }

    protected File getCommandOutput(CommandResult commandResult) throws IOException
    {
        File output = super.getCommandOutput(commandResult);

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
