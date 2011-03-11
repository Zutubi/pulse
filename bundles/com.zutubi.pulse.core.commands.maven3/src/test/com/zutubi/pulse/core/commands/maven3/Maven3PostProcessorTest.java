package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.postprocessors.DefaultPostProcessorFactory;
import com.zutubi.pulse.core.postprocessors.PostProcessorTestBase;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;

public class Maven3PostProcessorTest extends PostProcessorTestBase
{
    private Maven3PostProcessorConfiguration config;

    public void setUp() throws IOException
    {
        super.setUp();
        config = new Maven3PostProcessorConfiguration("maven3.pp");
    }

    private Maven3PostProcessor createProcessor()
    {
        DefaultPostProcessorFactory postProcessorFactory = new DefaultPostProcessorFactory();
        postProcessorFactory.setObjectFactory(new DefaultObjectFactory());

        Maven3PostProcessor pp = new Maven3PostProcessor(config);
        pp.setPostProcessorFactory(postProcessorFactory);
        return pp;
    }

    public void testSuccess() throws Exception
    {
        CommandResult result = createAndProcessArtifact("success", createProcessor());
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testWarnings() throws Exception
    {
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertWarnings("[INFO] --- maven-resources-plugin:2.4.3:resources (default-resources) @ app ---\n" +
                "[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!\n" +
                "[INFO] skip non existing resourceDirectory /home/jsankey/app/src/main/resources\n" +
                "[INFO] ",

                "[INFO] --- maven-compiler-plugin:2.3.2:compile (default-compile) @ app ---\n" +
                        "[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!\n" +
                        "[INFO] Compiling 1 source file to /home/jsankey/app/target/classes\n" +
                        "[INFO] ",

                "[INFO] --- maven-resources-plugin:2.4.3:testResources (default-testResources) @ app ---\n" +
                        "[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!\n" +
                        "[INFO] skip non existing resourceDirectory /home/jsankey/app/src/test/resources\n" +
                        "[INFO] ",

                "[INFO] --- maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ app ---\n" +
                        "[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!\n" +
                        "[INFO] Compiling 1 source file to /home/jsankey/app/target/test-classes\n" +
                        "[INFO] ");
    }

    public void testSuppressAllWarnings() throws Exception
    {
        config.getSuppressedWarnings().add(".*");
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertEquals(0, artifact.getFeatures().size());
    }

    public void testSuppressWarning() throws Exception
    {
        // Turn off context because it makes it difficult to see the right
        // warning is suppressed.
        config.setLeadingContext(0);
        config.setTrailingContext(0);
        
        config.getSuppressedWarnings().add("Using platform encoding");
        CommandResult result = createAndProcessArtifact("warnings", createProcessor());
        assertTrue(result.succeeded());
        assertWarnings("[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!",
                "[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!");
    }

    public void testNoPOM() throws Exception
    {
        createAndProcessArtifact("nopom", createProcessor());
        assertErrors("[INFO] ------------------------------------------------------------------------\n" +
                     "[INFO] BUILD FAILURE\n" +
                     "[INFO] ------------------------------------------------------------------------\n" +
                     "[INFO] Total time: 0.098s",
                
                     "[INFO] ------------------------------------------------------------------------\n" +
                     "[ERROR] The goal you specified requires a project to execute but there is no POM in this directory (/home/jsankey/app). Please verify you invoked Maven from the correct directory. -> [Help 1]\n" +
                     "[ERROR] \n" +
                     "[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n" +
                     "[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n" +
                     "[ERROR] \n" +
                     "[ERROR] For more information about the errors and possible solutions, please read the following articles:\n" +
                     "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MissingProjectException");
    }

    public void testNoGoal() throws Exception
    {
        createAndProcessArtifact("nogoal", createProcessor());
        assertErrors("[INFO] ------------------------------------------------------------------------\n" +
                "[INFO] BUILD FAILURE\n" +
                "[INFO] ------------------------------------------------------------------------\n" +
                "[INFO] Total time: 0.101s",

                "[INFO] ------------------------------------------------------------------------\n" +
                        "[ERROR] No goals have been specified for this build. You must specify a valid lifecycle phase or a goal in the format <plugin-prefix>:<goal> or <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>. Available lifecycle phases are: validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy, pre-site, site, post-site, site-deploy, pre-clean, clean, post-clean. -> [Help 1]\n" +
                        "[ERROR] \n" +
                        "[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n" +
                        "[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n" +
                        "[ERROR] \n" +
                        "[ERROR] For more information about the errors and possible solutions, please read the following articles:\n" +
                        "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/NoGoalSpecifiedException");
    }

    public void testCompilerError() throws Exception
    {
        createAndProcessArtifact("compilererror", createProcessor());
        assertErrors("[INFO] -------------------------------------------------------------\n" +
                "[ERROR] COMPILATION ERROR : \n" +
                "[INFO] -------------------------------------------------------------\n" +
                "[ERROR] /home/jsankey/app/src/main/java/com/zutubi/maven3/test/App.java:[11,18] cannot find symbol\n" +
                "symbol  : method printl(java.lang.String)\n" +
                "location: class java.io.PrintStream",

                "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] BUILD FAILURE\n" +
                        "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] Total time: 0.911s",

                "[INFO] ------------------------------------------------------------------------\n" +
                        "[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:2.3.2:compile (default-compile) on project app: Compilation failure\n" +
                        "[ERROR] /home/jsankey/app/src/main/java/com/zutubi/maven3/test/App.java:[11,18] cannot find symbol\n" +
                        "[ERROR] symbol  : method printl(java.lang.String)\n" +
                        "[ERROR] location: class java.io.PrintStream\n" +
                        "[ERROR] -> [Help 1]\n" +
                        "[ERROR] \n" +
                        "[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n" +
                        "[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n" +
                        "[ERROR] \n" +
                        "[ERROR] For more information about the errors and possible solutions, please read the following articles:\n" +
                        "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException");
    }

    public void testFatalError() throws Exception
    {
        createAndProcessArtifact("fatalerror", createProcessor());
        assertErrors("[INFO] Scanning for projects...\n" +
                "[ERROR] The build could not read 1 project -> [Help 1]\n" +
                "[ERROR]   \n" +
                "[ERROR]   The project  (/home/jsankey/app/nosuchpom.xml) has 1 error\n" +
                "[ERROR]     Non-readable POM /home/jsankey/app/nosuchpom.xml: /home/jsankey/app/nosuchpom.xml (No such file or directory)\n" +
                "[ERROR] \n" +
                "[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n" +
                "[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n" +
                "[ERROR] \n" +
                "[ERROR] For more information about the errors and possible solutions, please read the following articles:\n" +
                "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/ProjectBuildingException");
    }

    public void testTestFailure() throws Exception
    {
        createAndProcessArtifact("testfailure", createProcessor());
        assertErrors("Running com.zutubi.maven3.test.AppTest\n" +
                "Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.013 sec <<< FAILURE!",

                "\n" +
                        "Tests run: 1, Failures: 1, Errors: 0, Skipped: 0",

                "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] BUILD FAILURE\n" +
                        "[INFO] ------------------------------------------------------------------------\n" +
                        "[INFO] Total time: 1.685s",

                "[INFO] ------------------------------------------------------------------------\n" +
                        "[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.7.2:test (default-test) on project app: There are test failures.\n" +
                        "[ERROR] \n" +
                        "[ERROR] Please refer to /home/jsankey/app/target/surefire-reports for the individual test results.\n" +
                        "[ERROR] -> [Help 1]\n" +
                        "[ERROR] \n" +
                        "[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.\n" +
                        "[ERROR] Re-run Maven using the -X switch to enable full debug logging.\n" +
                        "[ERROR] \n" +
                        "[ERROR] For more information about the errors and possible solutions, please read the following articles:\n" +
                        "[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException");
    }
}
