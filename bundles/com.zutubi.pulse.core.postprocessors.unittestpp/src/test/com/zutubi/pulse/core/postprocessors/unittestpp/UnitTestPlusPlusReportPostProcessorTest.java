/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
