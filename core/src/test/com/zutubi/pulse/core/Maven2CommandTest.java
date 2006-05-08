/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.*;
import java.util.List;

/**
 * <class-comment/>
 */
public class Maven2CommandTest extends PulseTestCase
{
    File baseDir;
    File outputDir;

    private boolean generateMode = false;

    public void setUp() throws IOException
    {
        baseDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".base");
        outputDir = FileSystemUtils.createTempDirectory(getClass().getName(), ".out");
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

    public void testNoTarget() throws Exception
    {
        Maven2Command command = new Maven2Command();
        failedRun("basic", command, "BUILD FAILURE", "You must specify at least one goal");
    }

    public void testMultiGoal() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile test");
        successRun("basic", command, "BUILD SUCCESSFUL", "[surefire] Running com.zutubi.maven2.test.AppTest",
                "task-segment: [compile, test]", "[compiler:compile]", "[compiler:testCompile]");
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
        assertEquals(2, features.size());
        assertEquals("[surefire] Running com.zutubi.maven2.test.AppTest\n" +
                "[surefire] Tests run: 1, Failures: 1, Errors: 0, Time elapsed: x sec <<<<<<<< FAILURE !! ",
                features.get(0).getSummary().replaceAll("elapsed: .* sec", "elapsed: x sec"));
    }

    private CommandResult successRun(String inName, Maven2Command command, String ...contents) throws Exception
    {
        CommandResult result = runMaven(inName, command);
        assertTrue(result.succeeded());
        checkOutput(result, contents);
        return result;
    }

    private CommandResult failedRun(String inName, Maven2Command command, String ...contents) throws Exception
    {
        CommandResult result = runMaven(inName, command);
        assertTrue(result.failed());
        checkOutput(result, contents);
        return result;
    }

    private CommandResult runMaven(String inName, Maven2Command command) throws Exception
    {
        File sourceDir = getSource(inName);
        FileSystemUtils.removeDirectory(baseDir);
        FileSystemUtils.copyRecursively(sourceDir, baseDir);

        // Remove the .in extension from all files
        removeInExtension(baseDir);

        CommandResult result = new CommandResult("maven2-test");
        command.execute(0, new SimpleRecipePaths(baseDir, null), outputDir, result);

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

    protected void checkOutput(CommandResult commandResult, String ...contents) throws IOException
    {
        FileInputStream is = null;
        try
        {
            is = new FileInputStream(cleanOutput());
            String output = IOUtils.inputStreamToString(is);
            for (String content : contents)
            {
                if (!output.contains(content))
                {
                    fail("Output '" + output + "' does not contain '" + content + "'");
                }
            }
        }
        finally
        {
            IOUtils.close(is);
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
