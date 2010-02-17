package com.zutubi.pulse.core.postprocessors.unittestpp;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.FAILURE;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.PASS;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class UnitTestPlusPlusReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    public void testBasic() throws Exception
    {
        TestSuiteResult expected = buildSuite(null,
                buildSuite("DefaultSuite", 0,
                        new TestCaseResult("SuiteLess", 0, PASS)
                ),
                buildSuite("SuiteOne", 108,
                        new TestCaseResult("TestOne", 0, PASS),
                        new TestCaseResult("TestTwo", 1, FAILURE, "utpp.cpp(14) : false"),
                        new TestCaseResult("TestThrow", 107, FAILURE, "utpp.cpp(17) : Unhandled exception: Crash!")
                ),
                buildSuite("SuiteTwo", 0,
                        new TestCaseResult("TestOne", 0, PASS)
                )
        );

        assertEquals(expected, runProcessorAndGetTests(new UnitTestPlusPlusReportPostProcessor(new UnitTestPlusPlusReportPostProcessorConfiguration())));
    }

    public void testRandomJunkIgnored() throws IOException
    {
        TestSuiteResult expected = buildSuite(null,
                buildSuite("DefaultSuite", 0,
                        new TestCaseResult("SuiteLess", 0, PASS)
                ),
                buildSuite("SuiteOne", 108,
                        new TestCaseResult("TestOne", 0, PASS),
                        new TestCaseResult("TestTwo", 1, FAILURE, "utpp.cpp(14) : false"),
                        new TestCaseResult("TestThrow", 107, FAILURE, "utpp.cpp(17) : Unhandled exception: Crash!")
                ),
                buildSuite("SuiteTwo", 0,
                        new TestCaseResult("TestOne", 0, PASS)
                )
        );

        assertEquals(expected, runProcessorAndGetTests(new UnitTestPlusPlusReportPostProcessor(new UnitTestPlusPlusReportPostProcessorConfiguration())));
    }
}
