package com.zutubi.pulse.model;

import com.zutubi.pulse.util.FileSystemUtils;

/**
 */
public class AntPulseFileDetailsTest extends TemplatePulseFileDetailsTestBase
{
    private AntPulseFileDetails details;

    protected void setUp() throws Exception
    {
        details = new AntPulseFileDetails();
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        FileSystemUtils.rmdir(tmpDir);
    }

    public TemplatePulseFileDetails getDetails()
    {
        return details;
    }

    public void testBasic() throws Exception
    {
        createAndVerify("basic");
    }

    public void testExplicitBuildFile() throws Exception
    {
        details.setBuildFile("test.xml");
        createAndVerify("explicitBuildFile");
    }

    public void testEnvironment() throws Exception
    {
        details.addEnvironmentalVariable("var", "value");
        details.addEnvironmentalVariable("var2", "value2");
        createAndVerify("environment");
    }

    public void testExplicitTargets() throws Exception
    {
        details.setTargets("build test");
        createAndVerify("explicitTargets");
    }


    public void testExplicitWorkingDir() throws Exception
    {
        details.setWorkingDir("mywork");
        createAndVerify("explicitWorkingDir");
    }

    public void testExplicitArgs() throws Exception
    {
        details.setArguments("arg1 arg2");
        createAndVerify("explicitArgs");
    }

    public void testCaptureFile() throws Exception
    {
        FileCapture capture = new FileCapture("test name", "test file");
        details.addCapture(capture);
        createAndVerify("captureFile");
    }

    public void testCaptureFileType() throws Exception
    {
        FileCapture capture = new FileCapture("test name", "test file", "test mime type");
        details.addCapture(capture);
        createAndVerify("captureFileType");
    }

    public void testCaptureFileProcessor() throws Exception
    {
        FileCapture capture = new FileCapture("JUnit XML report", "reports/TESTS-TestSuites.xml", "text/xml");
        capture.addProcessor("junit");
        details.addCapture(capture);
        createAndVerify("captureFileProcessor");
    }

    public void testCaptureFileMultiProcessor() throws Exception
    {
        FileCapture capture = new FileCapture("JUnit XML report", "reports/TESTS-TestSuites.xml", "text/xml");
        capture.addProcessor("junit");
        capture.addProcessor("junit");
        details.addCapture(capture);
        createAndVerify("captureFileMultiProcessor");
    }

    public void testCaptureDir() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports");
        details.addCapture(capture);
        createAndVerify("captureDir");
    }

    public void testCaptureDirBase() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports", "reports");
        details.addCapture(capture);
        createAndVerify("captureDirBase");
    }

    public void testCaptureDirType() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports", "reports", "text/xml");
        details.addCapture(capture);
        createAndVerify("captureDirType");
    }

    public void testCaptureDirInclude() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports", "reports", "text/xml");
        capture.setIncludes("foo/**/*.xml");
        details.addCapture(capture);
        createAndVerify("captureDirInclude");
    }

    public void testCaptureDirExcludes() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports", "reports", "text/xml");
        capture.setExcludes("**/BadTest.xml   **/ReallyBadTest.xml");
        details.addCapture(capture);
        createAndVerify("captureDirExcludes");
    }

    public void testCaptureDirProcess() throws Exception
    {
        DirectoryCapture capture = new DirectoryCapture("JUnit XML reports", "reports", "text/xml");
        capture.addProcessor("junit");
        details.addCapture(capture);
        createAndVerify("captureDirProcess");
    }

}
