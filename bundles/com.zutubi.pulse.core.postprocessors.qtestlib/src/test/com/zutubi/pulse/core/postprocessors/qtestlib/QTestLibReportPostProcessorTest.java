package com.zutubi.pulse.core.postprocessors.qtestlib;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class QTestLibReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private QTestLibReportPostProcessor pp;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        pp = new QTestLibReportPostProcessor(new QTestLibReportPostProcessorConfiguration());
    }

    public void testDataDriven() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("DataDriven",
                    new TestCaseResult("initTestCase", PASS),
                    new TestCaseResult("toUpper", PASS),
                    new TestCaseResult("cleanupTestCase", PASS)
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp));
    }

    public void testAsserts() throws IOException
    {
        assertEquals(getFullSuite(), runProcessorAndGetTests(pp));
    }

    public void testVerbose() throws IOException
    {
        assertEquals(getFullSuite(), runProcessorAndGetTests(pp));
    }

    public void testRandomJunk() throws IOException
    {
        assertEquals(getFullSuite(), runProcessorAndGetTests(pp));
    }

    private TestSuiteResult getFullSuite()
    {
        return buildSuite(null,
                buildSuite("TestQString",
                    new TestCaseResult("initTestCase", PASS),
                new TestCaseResult("toUpper", PASS),
                new TestCaseResult("compareFail", -1, FAILURE, "testqstring.cpp:24: Compared values are not the same\n" +
                        "   Actual (str.toUpper()): HELLO\n" +
                        "   Expected (QString(\"HELO\")): HELO"),
                new TestCaseResult("skippy", -1, SKIPPED, "testqstring.cpp:29: This test requires more than is on offer"),
                new TestCaseResult("warnly", -1, PASS, "Warning: I'm mildy worried."),
                new TestCaseResult("cleanupTestCase", PASS)
            )
        );
    }
}
