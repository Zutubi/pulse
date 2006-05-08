/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.Feature;

import java.io.*;
import java.net.URISyntaxException;
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
        successRun("basic", "basic", command);
    }

    public void testNoTarget() throws Exception
    {
        Maven2Command command = new Maven2Command();
        failedRun("basic", "nogoal", command);
    }

    public void testMultiGoal() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile test");
        successRun("basic", "multigoal", command);
    }

    public void testNoPOM() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun("nopom", "nopom", command);
    }

    public void testCompilerError() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("compile");
        failedRun("compilererror", "compilererror", command);
    }

    public void testTestFailure() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        failedRun("testfailure", "testfailure", command);
    }

    public void testAppliesProcessor() throws Exception
    {
        Maven2Command command = new Maven2Command();
        command.setGoals("test");
        CommandResult result = failedRun("testfailure", "testfailure", command);
        List<Feature> features = result.getArtifact("command output").getFeatures(Feature.Level.ERROR);
        assertEquals(2, features.size());
        assertEquals("[surefire] Running com.zutubi.maven2.test.AppTest\n" +
                "[surefire] Tests run: 1, Failures: 1, Errors: 0, Time elapsed: x sec <<<<<<<< FAILURE !! ",
                features.get(0).getSummary().replaceAll("elapsed: .* sec", "elapsed: x sec"));
    }

    private CommandResult successRun(String inName, String outName, Maven2Command command) throws Exception
    {
        CommandResult result = runMaven(inName, outName, command);
        assertTrue(result.succeeded());
        return result;
    }

    private CommandResult failedRun(String inName, String outName, Maven2Command command) throws Exception
    {
        CommandResult result = runMaven(inName, outName, command);
        assertTrue(result.failed());
        return result;
    }

    private CommandResult runMaven(String inName, String outName, Maven2Command command) throws Exception
    {
        File sourceDir = getSource(inName);
        FileSystemUtils.removeDirectory(baseDir);
        FileSystemUtils.copyRecursively(sourceDir, baseDir);

        // Remove the .in extension from all files
        removeInExtension(baseDir);

        CommandResult result = new CommandResult("maven2-test");
        command.execute(0, new SimpleRecipePaths(baseDir, null), outputDir, result);

        compareOutput(outName);
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

    private void compareOutput(String name) throws Exception
    {
        File actualOutput = cleanOutput();
        File expectedOutput = new File(getSource(name).getAbsolutePath() + ".txt");

        if(generateMode)
        {
            IOUtils.copyFile(actualOutput, expectedOutput);
        }
        else
        {
            assertFilesEqual(expectedOutput, actualOutput);
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
