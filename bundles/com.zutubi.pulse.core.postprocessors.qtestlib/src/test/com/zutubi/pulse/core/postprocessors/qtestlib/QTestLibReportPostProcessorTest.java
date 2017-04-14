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

    public void testFull() throws IOException
    {
        TestSuiteResult expected =
            buildSuite(null,
                buildSuite("tst_QFontComboBox",
                    new TestCaseResult("initTestCase", PASS),
                    new TestCaseResult("qfontcombobox", PASS),
                        new TestCaseResult("currentFont", -1, FAILURE, "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:152: Data Tag: 'default': Compared values are not the same\n" +
                                "   Actual (boxFontFamily): Arial\n" +
                                "   Expected (currentFont.family()): Sans Serif\n" +
                                "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:147: Data Tag: 'default': Compared values are not the same\n" +
                                "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:147: Data Tag: 'Courier': Compared values are not the same"),
                        new TestCaseResult("sizeHint", PASS),
                        new TestCaseResult("writingSystem", -1, FAILURE, "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:277: Data Tag: 'Any': Compared values are not the same\n" +
                                "   Actual (spy0.count()): 2\n" +
                                "   Expected ((currentFont != box.currentFont()) ? 1 : 0): 1\n" +
                                "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:277: Data Tag: 'Latin': Compared values are not the same\n" +
                                "   Actual (spy0.count()): 2\n" +
                                "   Expected ((currentFont != box.currentFont()) ? 1 : 0): 1\n" +
                                "/home/cduclos/dev/qt-4.6/tests/auto/qfontcombobox/tst_qfontcombobox.cpp:277: Data Tag: 'enum': Compared values are not the same\n" +
                                "   Actual (spy0.count()): 2\n" +
                                "   Expected ((currentFont != box.currentFont()) ? 1 : 0): 1"),
                        new TestCaseResult("currentFontChanged", PASS),
                        new TestCaseResult("cleanupTestCase", PASS)
                )
            );

        assertEquals(expected, runProcessorAndGetTests(pp));
    }
}
