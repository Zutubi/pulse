package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.*;

import java.io.IOException;
import java.util.List;

import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class RegexTestPostProcessorTest extends TestPostProcessorTestCase
{
    private static final String EXTENSION = "txt";

    public void testSmokeTest() throws IOException
    {
        TestSuiteResult tests = runProcessorAndGetTests(createProcessor(), EXTENSION);
        assertEquals(5, tests.getTotalWithStatus(FAILURE));
        assertEquals(91, tests.getTotal());
        assertEquals(0, tests.getTotalWithStatus(ERROR));
    }

    public void testAutoFail() throws IOException
    {
        TestSuiteResult tests = runProcessorAndGetTests(createProcessor(true, -1), EXTENSION);
        assertEquals(5, tests.getTotal());
        assertEquals(3, tests.getTotalWithStatus(FAILURE));
        assertEquals(1, tests.getTotalWithStatus(ERROR));
        assertEquals(PASS, tests.findCase("test1").getStatus());
        assertEquals(ERROR, tests.findCase("test2").getStatus());
        assertEquals(FAILURE, tests.findCase("test3").getStatus());
        assertEquals(FAILURE, tests.findCase("test4").getStatus());
        assertEquals(FAILURE, tests.findCase("test5").getStatus());
    }

    public void testUnrecognised() throws IOException
    {
        TestSuiteResult tests = runProcessorAndGetTests(createProcessor(), EXTENSION);
        assertEquals(3, tests.getTotal());
        assertEquals(1, tests.getTotalWithStatus(FAILURE));
        assertEquals(1, tests.getTotalWithStatus(ERROR));
        assertEquals(PASS, tests.findCase("test1").getStatus());
        assertEquals(ERROR, tests.findCase("test2").getStatus());
        assertEquals(FAILURE, tests.findCase("test4").getStatus());
        assertNull(tests.findCase("test3"));
        assertNull(tests.findCase("test5"));
    }

    public void testDetails() throws IOException
    {
        TestSuiteResult tests = runProcessorAndGetTests(createProcessor(false, 3), EXTENSION);
        assertEquals(4, tests.getTotal());
        assertEquals(2, tests.getTotalWithStatus(FAILURE));
        assertEquals("fail 1 details", tests.findCase(" <FAIL1>").getMessage());
        assertEquals("fail 2 details", tests.findCase(" <FAIL2>").getMessage());
    }

    public void testMultipleStatuses() throws IOException
    {
        RegexTestPostProcessor pp = createProcessor(false, 3);
        pp.getConfig().setPassStatus(asList("PASS", "GOOD"));
        pp.getConfig().setFailureStatus(asList("FAIL", "POOR"));
        
        TestSuiteResult tests = runProcessorAndGetTests(pp, EXTENSION);
        assertEquals(4, tests.getTotal());
        assertEquals(2, tests.getTotalWithStatus(FAILURE));
    }
    
    public void testSkipped() throws IOException
    {
        TestSuiteResult tests = runProcessorAndGetTests(createProcessor(), EXTENSION);
        TestCaseResult skippedCase = tests.findCase(" <SKIPPY>");
        assertNotNull(skippedCase);
        assertEquals(TestStatus.SKIPPED, skippedCase.getStatus());
    }

    public void testSuiteGroups() throws IOException
    {
        // TEST INFO: Test suite: TestPSQLA - Test case: testQuery01 - Test detail: psql: Setup (3.88 sec) - Test Status: PASS
        RegexTestPostProcessorConfiguration pp = new RegexTestPostProcessorConfiguration();
        pp.setRegex("TEST INFO: Test suite: (.*) - Test case: (.*) - Test detail: (.*) - Test Status: (.*)");
        pp.setSuiteGroup(1);
        pp.setNameGroup(2);
        pp.setDetailsGroup(3);
        pp.setStatusGroup(4);
        pp.setPassStatus(asList("PASS"));
        pp.setSuite("baseSuite");

        RegexTestPostProcessor postProcessor = new RegexTestPostProcessor(pp);

        TestSuiteResult tests = runProcessorAndGetTests(postProcessor, EXTENSION);
        TestSuiteResult baseSuite = tests.getSuites().get(0);
        assertEquals("baseSuite", baseSuite.getName());

        List<TestSuiteResult> suites = baseSuite.getSuites();
        assertEquals(3, suites.size());

        TestSuiteResult suiteA = suites.get(0);
        assertEquals("TestPSQLA", suiteA.getName());
        assertEquals(2, suiteA.getCases().size());

        TestSuiteResult suiteB = suites.get(1);
        assertEquals("TestPSQLB", suiteB.getName());
        assertEquals(1, suiteB.getCases().size());

        TestSuiteResult suiteC = suites.get(2);
        assertEquals("TestPSQLC", suiteC.getName());
        assertEquals(2, suiteC.getCases().size());
    }

    public void testDurationGroups() throws IOException
    {
        // TEST INFO: Test suite: TestPSQLA - Test case: testQuery01 - Test duration: 388 - Test Status: PASS
        RegexTestPostProcessorConfiguration pp = new RegexTestPostProcessorConfiguration();
        pp.setRegex("TEST INFO: Test case: (.*) - Test duration: (.*) - Test Status: (.*)");
        pp.setNameGroup(1);
        pp.setDurationGroup(2);
        pp.setStatusGroup(3);
        pp.setPassStatus(asList("PASS"));

        RegexTestPostProcessor postProcessor = new RegexTestPostProcessor(pp);

        TestSuiteResult tests = runProcessorAndGetTests(postProcessor, EXTENSION);
        tests.getDuration();

        List<TestCaseResult> cases = tests.getCases();
        assertEquals(3, cases.size());
    }

    public void testNoCaseName() throws IOException
    {
        RegexTestPostProcessorConfiguration config = new RegexTestPostProcessorConfiguration();
        config.setRegex("(?:(a.*)|nota.*): (.*)");
        config.setNameGroup(1);
        config.setStatusGroup(2);
        
        RegexTestPostProcessor pp = new RegexTestPostProcessor(config);
        TestPostProcessorContext context = runProcessor(pp, EXTENSION);
        TestSuiteResult tests = context.getTestSuiteResult();
        assertEquals(1, tests.getTotal());
        assertNull(tests.findCase("notaname"));
        assertNotNull(tests.findCase("aname"));

        List<Feature> features = context.getFeatures();
        assertEquals(1, features.size());
        
        Feature feature = features.get(0);
        assertEquals(Feature.Level.WARNING, feature.getLevel());
        assertThat(feature.getSummary(), containsString("no test case name"));
    }

    private RegexTestPostProcessor createProcessor()
    {
        return createProcessor(false, -1);
    }

    private RegexTestPostProcessor createProcessor(boolean autoFail, int detailsGroup)
    {
        RegexTestPostProcessorConfiguration pp = new RegexTestPostProcessorConfiguration();
        pp.setRegex("\\[(.*)\\] .*EDT:([^:]*)(?:\\: (.*))?");
        pp.setStatusGroup(1);
        pp.setNameGroup(2);
        pp.setDetailsGroup(detailsGroup);
        pp.setPassStatus(asList("PASS"));
        pp.setFailureStatus(asList("FAIL"));

        pp.setAutoFail(autoFail);
        return new RegexTestPostProcessor(pp);
    }
}
