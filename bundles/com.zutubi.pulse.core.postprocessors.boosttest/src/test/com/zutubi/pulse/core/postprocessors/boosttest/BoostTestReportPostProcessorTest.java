package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class BoostTestReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    public void testBasic() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("PulseTest", 0,
                    buildSuite("VariantsSuite", 0,
                        new TestCaseResult("simplePass", 0, PASS),
                        new TestCaseResult("checkFailure", 0, FAILURE, "error: main.cpp:20: check add(2, 2) == 5 failed"),
                        new TestCaseResult("multipleCheckFailures", 0, FAILURE, "error: main.cpp:25: check add(2, 2) == 1 failed\nerror: main.cpp:26: check add(2, 2) == 2 failed\nerror: main.cpp:27: check add(2, 2) == 3 failed"),
                        new TestCaseResult("requireFailure", 0, FAILURE, "fatal error: main.cpp:32: critical check add(2, 2) == 5 failed"),
                        new TestCaseResult("explicitError", 0, FAILURE, "error: main.cpp:37: Error message"),
                        new TestCaseResult("explicitFailure", 0, FAILURE, "fatal error: main.cpp:42: Failure message"),
                        new TestCaseResult("errorThenFailure", 0, FAILURE, "fatal error: main.cpp:47: Error message"),
                        new TestCaseResult("uncaughtException", 0, ERROR, "uncaught exception: C string: Catch me if you can!"),
                        new TestCaseResult("stdException", 0, ERROR, "uncaught exception: unknown type"),
                        new TestCaseResult("checkMessageFailure", 0, FAILURE, "error: main.cpp:63: add(..) result: 4"),
                        new TestCaseResult("checkEqualFailure", 0, FAILURE, "error: main.cpp:68: check add( 2,2 ) == 5 failed [4 != 5]"),
                        new TestCaseResult("threeSeconds", 0, PASS)
                    ),
                    buildSuite("PassingSuite", 0,
                        new TestCaseResult("pass1", 0, PASS),
                        new TestCaseResult("pass2", 0, PASS),
                        new TestCaseResult("pass3", 0, PASS)
                    )
                )
            );
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(new BoostTestReportPostProcessorConfiguration())));
    }

    public void testRandomJunkIgnored() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("PulseTest", 0,
                    buildSuite("VariantsSuite", 0,
                        new TestCaseResult("simplePass", 0, PASS),
                        new TestCaseResult("checkFailure", 0, FAILURE, "error: main.cpp:20: check add(2, 2) == 5 failed"),
                        new TestCaseResult("multipleCheckFailures", 0, FAILURE, "error: main.cpp:25: check add(2, 2) == 1 failed\nerror: main.cpp:26: check add(2, 2) == 2 failed\nerror: main.cpp:27: check add(2, 2) == 3 failed"),
                        new TestCaseResult("requireFailure", 0, FAILURE, "fatal error: main.cpp:32: critical check add(2, 2) == 5 failed"),
                        new TestCaseResult("explicitError", 0, FAILURE, "error: main.cpp:37: Error message"),
                        new TestCaseResult("explicitFailure", 0, FAILURE, "fatal error: main.cpp:42: Failure message"),
                        new TestCaseResult("errorThenFailure", 0, FAILURE, "fatal error: main.cpp:47: Error message"),
                        new TestCaseResult("uncaughtException", 0, ERROR, "uncaught exception: C string: Catch me if you can!"),
                        new TestCaseResult("stdException", 0, ERROR, "uncaught exception: unknown type"),
                        new TestCaseResult("checkMessageFailure", 0, FAILURE, "error: main.cpp:63: add(..) result: 4"),
                        new TestCaseResult("checkEqualFailure", 0, FAILURE, "error: main.cpp:68: check add( 2,2 ) == 5 failed [4 != 5]"),
                        new TestCaseResult("threeSeconds", 0, PASS)
                    ),
                    buildSuite("PassingSuite", 0,
                        new TestCaseResult("pass1", 0, PASS),
                        new TestCaseResult("pass2", 0, PASS),
                        new TestCaseResult("pass3", 0, PASS)
                    )
                )
            );
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(new BoostTestReportPostProcessorConfiguration())));
    }

    public void testDurations() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("Outer", 259,
                    buildSuite("Nested1", 204,
                        new TestCaseResult("nested1Case1", 203, PASS),
                        new TestCaseResult("nested1Case2", 1, PASS)
                    ),
                    buildSuite("Nested2", 2,
                        new TestCaseResult("nested2Case1", 2, PASS)
                    ),
                    new TestCaseResult("directCase1", 3, PASS),
                    new TestCaseResult("directCase2", 50, PASS)
                )
            );
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(new BoostTestReportPostProcessorConfiguration())));
    }

    public void testMessages() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("suite", 0,
                    new TestCaseResult("pass", 0, PASS, "info: suites.cpp:27: check e == m * c * c passed"),
                    new TestCaseResult("message", 0, PASS, "message: suites.cpp:32: This is a message"),
                    new TestCaseResult("passpassfail", 0, FAILURE, "info: suites.cpp:37: check 1 == 1 passed\ninfo: suites.cpp:39: check 2 == 2 passed\nerror: suites.cpp:40: check 1 == 2 failed")
                )
            );

        BoostTestReportPostProcessorConfiguration config = new BoostTestReportPostProcessorConfiguration();
        config.setProcessInfo(true);
        config.setProcessMessages(true);
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(config)));
    }

    public void testMessagesDefaultSettings() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("suite", 0,
                    new TestCaseResult("pass", 0, PASS),
                    new TestCaseResult("message", 0, PASS),
                    new TestCaseResult("passpassfail", 0, FAILURE, "error: suites.cpp:40: check 1 == 2 failed")
                )
            );

        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(new BoostTestReportPostProcessorConfiguration()), "testMessages", EXTENSION_XML));
    }

    public void testMessagesNoInfo() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("suite", 0,
                    new TestCaseResult("pass", 0, PASS),
                    new TestCaseResult("message", 0, PASS, "message: suites.cpp:32: This is a message"),
                    new TestCaseResult("passpassfail", 0, FAILURE, "error: suites.cpp:40: check 1 == 2 failed")
                )
            );

        BoostTestReportPostProcessorConfiguration config = new BoostTestReportPostProcessorConfiguration();
        config.setProcessMessages(true);
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(config), "testMessages", EXTENSION_XML));
    }
}
