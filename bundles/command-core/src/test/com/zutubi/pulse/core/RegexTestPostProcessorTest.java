package com.zutubi.pulse.core;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * <class comment/>
 */
public class RegexTestPostProcessorTest extends PulseTestCase
{
    private File tmpDir = null;
    private StoredFileArtifact artifact = null;
    private CommandResult result = null;

    public RegexTestPostProcessorTest()
    {
    }

    public RegexTestPostProcessorTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        tmpDir = FileSystemUtils.createTempDir("RegexTestPostProcessorTest", getName());

        artifact = prepareArtifact(this.getName());

        result = new CommandResult("output");
    }

    protected void tearDown() throws Exception
    {
        artifact = null;
        removeDirectory(tmpDir);

        super.tearDown();
    }

    private StoredFileArtifact prepareArtifact(String name) throws IOException
    {
        File tmpFile = new File(tmpDir, name + ".txt");
        IOUtils.joinStreams(
                this.getClass().getResourceAsStream("RegexTestPostProcessorTest."+name+".txt"),
                new FileOutputStream(tmpFile),
                true
        );

        return new StoredFileArtifact( name + ".txt");
    }

    public void testSmokeTest()
    {
        TestSuiteResult tests = process();
        assertEquals(5, tests.getFailures());
        assertEquals(91, tests.getTotal());
        assertEquals(0, tests.getErrors());
    }

    private TestSuiteResult process()
    {
        RegexTestPostProcessor pp = new RegexTestPostProcessor();
        pp.setRegex("\\[(.*)\\] .*EDT:(.*)");
        pp.setStatusGroup(1);
        pp.setNameGroup(2);
        pp.setPassStatus("PASS");
        pp.setFailureStatus("FAIL");

        TestSuiteResult testResults = new TestSuiteResult();
        RecipeContext recipeContext = new RecipeContext();
        recipeContext.setTestResults(testResults);
        CommandContext context = new CommandContext();
        context.setOutputDir(tmpDir);
        context.setRecipeContext(recipeContext);
        
        pp.process(artifact, result, context);
        return testResults;
    }    
}
