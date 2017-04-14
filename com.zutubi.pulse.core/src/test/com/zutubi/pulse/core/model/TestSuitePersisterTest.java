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

package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.io.FileSystemUtils;
import nu.xom.ParsingException;

import java.io.File;
import java.io.IOException;

import static com.zutubi.pulse.core.postprocessors.api.TestStatus.*;

public class TestSuitePersisterTest extends PulseTestCase
{
    private File tempDir;
    private TestSuitePersister persister;

    protected void setUp() throws Exception
    {
        super.setUp();
        tempDir = FileSystemUtils.createTempDir(TestSuitePersisterTest.class.getName(), "");
        persister = new TestSuitePersister();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testEmptySuite() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("name", 121);
        roundTrip(suite);
    }

    public void testSingleCase() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("suite", 100);
        suite.add(new PersistentTestCaseResult("case", 24, PASS, "this is the message"));
        roundTrip(suite);
    }

    public void testMultipleCases() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("asuite");
        suite.add(new PersistentTestCaseResult("case3", -1, FAILURE, null));
        suite.add(new PersistentTestCaseResult("case1", 24, PASS, "this is the message"));
        suite.add(new PersistentTestCaseResult("case2", 100, ERROR, null));
        roundTrip(suite);
    }

    public void testSkippedCases() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("asuite");
        suite.add(new PersistentTestCaseResult("case1", 24, PASS, "this is the message"));
        suite.add(new PersistentTestCaseResult("case2", 0, SKIPPED, null));
        PersistentTestSuiteResult nestedSuite = new PersistentTestSuiteResult("anestedsuite");
        nestedSuite.add(new PersistentTestCaseResult("case1", 100, ERROR, null));
        nestedSuite.add(new PersistentTestCaseResult("case2", 0, SKIPPED, null));
        suite.add(nestedSuite);
        roundTrip(suite);
    }

    public void testNestedSuite() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("asuite");
        PersistentTestSuiteResult nestedSuite = new PersistentTestSuiteResult("anestedsuite");
        nestedSuite.add(new PersistentTestCaseResult("case1", 100, ERROR, null));
        suite.add(new PersistentTestCaseResult("case1", -1, FAILURE, null));
        suite.add(nestedSuite);
        roundTrip(suite);
    }

    public void testComplexSuite() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("asuite");

        PersistentTestSuiteResult nestedNestedSuite = new PersistentTestSuiteResult("anestednestedsuite");
        nestedNestedSuite.add(new PersistentTestCaseResult("deep", 100000, FAILURE, "sorry\nit failed"));

        PersistentTestSuiteResult nestedSuite = new PersistentTestSuiteResult("anestedsuite");
        nestedSuite.add(new PersistentTestCaseResult("case1", 100, ERROR, null));
        nestedSuite.add(new PersistentTestCaseResult("anothercase", 1, ERROR, "with a messsage"));
        nestedSuite.add(nestedNestedSuite);
        nestedSuite.add(new PersistentTestCaseResult("yetanothercase", 1, PASS, "with another messsage"));

        suite.add(new PersistentTestCaseResult("case1", -1, FAILURE, null));
        suite.add(nestedSuite);
        roundTrip(suite);
    }

    public void testXMLSpecialCharacters() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("<>&'\"!<!-- -->");
        suite.add(new PersistentTestCaseResult("<>&'\"!<!-- -->", 1, PASS, "<>&'\"!<!-- -->"));

        PersistentTestSuiteResult outer = new PersistentTestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    public void testControlCharacters() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("\u0018");
        suite.add(new PersistentTestCaseResult("\u0018", 1, PASS, "\u0018"));

        PersistentTestSuiteResult outer = new PersistentTestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    public void testEmptyMessage() throws Exception
    {
        PersistentTestSuiteResult suite = new PersistentTestSuiteResult("suity");
        suite.add(new PersistentTestCaseResult("castor", 1, PASS, ""));

        PersistentTestSuiteResult outer = new PersistentTestSuiteResult("outer");
        outer.add(suite);
        roundTrip(outer);
    }

    private void roundTrip(PersistentTestSuiteResult suite) throws IOException, ParsingException
    {
        persister.write(suite, tempDir);
        PersistentTestSuiteResult otherSuite = persister.read(suite.getName(), tempDir, true, false, -1);
        assertTrue(suite.isEquivalent(otherSuite));
    }
}
