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

package com.zutubi.pulse.core.postprocessors.nunit;

import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.api.XMLTestPostProcessorTestCase;

import java.io.IOException;

public class NUnitReportPostProcessorTest extends XMLTestPostProcessorTestCase
{
    private NUnitReportPostProcessor pp;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        pp = new NUnitReportPostProcessor(new NUnitReportPostProcessorConfiguration());
    }

    public void testPassing() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("NUnit", 14,
                    buildSuite("Tests", 12,
                        buildSuite("PassingTest", 12,
                            new TestCaseResult("Empty", 3, PASS),
                            new TestCaseResult("Equals", 3, PASS),
                            new TestCaseResult("ExpectAnException", 1, PASS)
                        )
                    )
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp));
    }

    public void testSimpleCSharp() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("NUnit", 57,
                    buildSuite("Samples", 55,
                        buildSuite("SimpleCSharpTest", 54,
                            new TestCaseResult("Add", 34, FAILURE, "Expected Failure.\n" +
                                    "\texpected: <6>\n" +
                                    "\t but was: <5>\n" +
                                    "   at NUnit.Samples.SimpleCSharpTest.Add() in c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\AssertSyntaxTests.cs:line 66"),
                            new TestCaseResult("DivideByZero", 3, FAILURE, "System.DivideByZeroException : Attempted to divide by zero.\n" +
                                    "   at NUnit.Samples.SimpleCSharpTest.DivideByZero() in c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\AssertSyntaxTests.cs:line 76"),
                            new TestCaseResult("Equals", 2, FAILURE, "Expected Failure (Integer)\n" +
                                    "\texpected: <12>\n" +
                                    "\t but was: <13>\n" +
                                    "   at NUnit.Samples.SimpleCSharpTest.Equals() in c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\AssertSyntaxTests.cs:line 90"),
                            new TestCaseResult("ExpectAnException", 0, FAILURE, "Expected: System.InvalidOperationException but was System.InvalidCastException\n" +
                                    "   at NUnit.Samples.SimpleCSharpTest.ExpectAnException() in c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\AssertSyntaxTests.cs:line 98"),
                            new TestCaseResult("IgnoredTest", -1, SKIPPED, "ignored test")
                        )
                    )
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp));
    }

    public void testRandomJunkIgnored() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("NUnit", 57,
                    buildSuite("Samples", 55,
                        buildSuite("SimpleCSharpTest", 54,
                            new TestCaseResult("Add", 34, FAILURE, "Expected Failure.\n" +
                                    "\texpected: <6>\n" +
                                    "\t but was: <5>\n" +
                                    "   at NUnit.Samples.SimpleCSharpTest.Add() in c:\\tools\\cygwin\\home\\jsankey\\repo\\nant\\hello\\AssertSyntaxTests.cs:line 66"),
                            new TestCaseResult("IgnoredTest", -1, SKIPPED, "ignored test"),
                            new TestCaseResult("Empty", 3, PASS)
                        ),
                        buildSuite("SecondTest", 4,
                            new TestCaseResult("Passing", 3, PASS)
                        )
                    )
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp));
    }
}
