package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.NameConflictResolution;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.IOException;

public class PersistentTestSuiteResultTest extends PulseTestCase
{
    public void testConflictsAppend() throws IOException
    {
        PersistentTestSuiteResult tests = getTests(NameConflictResolution.APPEND);
        assertEquals(5, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>2"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>3"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    public void testConflictsOff() throws IOException
    {
        PersistentTestSuiteResult tests = getTests(NameConflictResolution.OFF);
        assertEquals(3, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    public void testConflictsPrepend() throws IOException
    {
        PersistentTestSuiteResult tests = getTests(NameConflictResolution.PREPEND);
        assertEquals(5, tests.getTotal());
        assertTrue(tests.hasCase(" <TEST COMMAND0>"));
        assertTrue(tests.hasCase(" <TEST COMMAND1>"));
        assertTrue(tests.hasCase("2 <TEST COMMAND1>"));
        assertTrue(tests.hasCase("3 <TEST COMMAND1>"));
        assertTrue(tests.hasCase(" <TEST COMMAND2>"));
    }

    private PersistentTestSuiteResult getTests(NameConflictResolution resolution)
    {
        TestSuiteResult suite = new TestSuiteResult("test");
        suite.addCase(new TestCaseResult(" <TEST COMMAND0>"));
        suite.addCase(new TestCaseResult(" <TEST COMMAND1>"));
        suite.addCase(new TestCaseResult(" <TEST COMMAND1>"));
        suite.addCase(new TestCaseResult(" <TEST COMMAND1>"));
        suite.addCase(new TestCaseResult(" <TEST COMMAND2>"));
        return new PersistentTestSuiteResult(suite, resolution);
    }
}
