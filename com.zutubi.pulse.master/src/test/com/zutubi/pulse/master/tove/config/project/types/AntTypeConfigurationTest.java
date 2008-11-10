package com.zutubi.pulse.master.tove.config.project.types;

public class AntTypeConfigurationTest extends TemplateTypeConfigurationTestBase
{
    private AntTypeConfiguration type = new AntTypeConfiguration();

    public TemplateTypeConfiguration getType()
    {
        return type;
    }

    public void testBasic() throws Exception
    {
        createAndVerify("basic");
    }

    public void testExplicitBuildFile() throws Exception
    {
        type.setFile("test.xml");
        createAndVerify("explicitBuildFile");
    }

    public void testExplicitTargets() throws Exception
    {
        type.setTarget("build test");
        createAndVerify("explicitTargets");
    }


    public void testExplicitWorkingDir() throws Exception
    {
        type.setWork("mywork");
        createAndVerify("explicitWorkingDir");
    }

    public void testExplicitArgs() throws Exception
    {
        type.setArgs("arg1 arg2");
        createAndVerify("explicitArgs");
    }

    public void testCaptureFile() throws Exception
    {
        FileArtifactConfiguration file = new FileArtifactConfiguration("test name", "test file");
        type.addArtifact(file);
        createAndVerify("captureFile");
    }

    public void testCaptureFileType() throws Exception
    {
        FileArtifactConfiguration file = new FileArtifactConfiguration("test name", "test file", "test mime type");
        type.addArtifact(file);
        createAndVerify("captureFileType");
    }

    public void testCaptureFileProcessor() throws Exception
    {
        FileArtifactConfiguration file = new FileArtifactConfiguration("JUnit XML report", "reports/TESTS-TestSuites.xml", "text/xml");
        file.addPostprocessor("junit");
        type.addArtifact(file);
        createAndVerify("captureFileProcessor");
    }

    public void testCaptureFileMultiProcessor() throws Exception
    {
        FileArtifactConfiguration fileArtifactConfiguration = new FileArtifactConfiguration("JUnit XML report", "reports/TESTS-TestSuites.xml", "text/xml");
        fileArtifactConfiguration.addPostprocessor("junit");
        fileArtifactConfiguration.addPostprocessor("junit");
        type.addArtifact(fileArtifactConfiguration);
        createAndVerify("captureFileMultiProcessor");
    }

    public void testCaptureDir() throws Exception
    {
        DirectoryArtifactConfiguration directoryArtifactConfiguration = new DirectoryArtifactConfiguration("JUnit XML reports");
        type.addArtifact(directoryArtifactConfiguration);
        createAndVerify("captureDir");
    }

    public void testCaptureDirBase() throws Exception
    {
        DirectoryArtifactConfiguration dir = new DirectoryArtifactConfiguration("JUnit XML reports", "reports");
        type.addArtifact(dir);
        createAndVerify("captureDirBase");
    }

    public void testCaptureDirType() throws Exception
    {
        DirectoryArtifactConfiguration dir = new DirectoryArtifactConfiguration("JUnit XML reports", "reports", "text/xml");
        type.addArtifact(dir);
        createAndVerify("captureDirType");
    }

    public void testCaptureDirInclude() throws Exception
    {
        DirectoryArtifactConfiguration dir = new DirectoryArtifactConfiguration("JUnit XML reports", "reports", "text/xml");
        dir.setIncludes("foo/**/*.xml");
        type.addArtifact(dir);
        createAndVerify("captureDirInclude");
    }

    public void testCaptureDirExcludes() throws Exception
    {
        DirectoryArtifactConfiguration dir = new DirectoryArtifactConfiguration("JUnit XML reports", "reports", "text/xml");
        dir.setExcludes("**/BadTest.xml   **/ReallyBadTest.xml");
        type.addArtifact(dir);
        createAndVerify("captureDirExcludes");
    }

    public void testCaptureDirProcess() throws Exception
    {
        DirectoryArtifactConfiguration dir = new DirectoryArtifactConfiguration("JUnit XML reports", "reports", "text/xml");
        dir.addPostprocessor("junit");
        type.addArtifact(dir);
        createAndVerify("captureDirProcess");
    }

}
