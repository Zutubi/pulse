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
                buildSuite("PulseTest",
                    buildSuite("VariantsSuite",
                        new TestCaseResult("simplePass", 0, PASS),
                        new TestCaseResult("checkFailure", 0, FAILURE, "error: main.cpp:20: check add(2, 2) == 5 failed"),
                        new TestCaseResult("multipleCheckFailures", 0, FAILURE, "error: main.cpp:25: check add(2, 2) == 1 failed\nerror: main.cpp:26: check add(2, 2) == 2 failed\nerror: main.cpp:27: check add(2, 2) == 3 failed"),
                        new TestCaseResult("requireFailure", 0, FAILURE, "fatalerror: main.cpp:32: critical check add(2, 2) == 5 failed"),
                        new TestCaseResult("explicitError", 0, FAILURE, "error: main.cpp:37: Error message"),
                        new TestCaseResult("explicitFailure", 0, FAILURE, "fatalerror: main.cpp:42: Failure message"),
                        new TestCaseResult("errorThenFailure", 0, FAILURE, "fatalerror: main.cpp:47: Error message"),
                        new TestCaseResult("uncaughtException", 0, ERROR, "uncaught exception: C string: Catch me if you can!"),
                        new TestCaseResult("stdException", 0, ERROR, "uncaught exception: unknown type"),
                        new TestCaseResult("checkMessageFailure", 0, FAILURE, "error: main.cpp:63: add(..) result: 4"),
                        new TestCaseResult("checkEqualFailure", 0, FAILURE, "error: main.cpp:68: check add( 2,2 ) == 5 failed [4 != 5]"),
                        new TestCaseResult("threeSeconds", 0, PASS)
                    ),
                    buildSuite("PassingSuite",
                        new TestCaseResult("pass1", 0, PASS),
                        new TestCaseResult("pass2", 0, PASS),
                        new TestCaseResult("pass3", 0, PASS)
                    )
                )
            );
        assertEquals(expected, runProcessorAndGetTests(new BoostTestReportPostProcessor(new BoostTestReportPostProcessorConfiguration())));
    }
}
