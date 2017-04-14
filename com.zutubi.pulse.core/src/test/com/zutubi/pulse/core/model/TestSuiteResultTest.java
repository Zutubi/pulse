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

import static com.zutubi.pulse.core.postprocessors.api.TestStatus.FAILURE;
import static com.zutubi.pulse.core.postprocessors.api.TestStatus.PASS;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.List;

/**
 */
public class TestSuiteResultTest extends PulseTestCase
{
    private PersistentTestSuiteResult suite;


    protected void setUp() throws Exception
    {
        suite = new PersistentTestSuiteResult("test", 10);
    }

    protected void tearDown() throws Exception
    {
        suite = null;
    }

    public void testAddCase()
    {
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 100, PASS, "test message");
        suite.add(childCase);
        assertEquals(1, suite.getTotal());
        assertTrue(suite.getCases().iterator().next() == childCase);
    }

    public void testAddSuite()
    {
        PersistentTestSuiteResult childSuite = new PersistentTestSuiteResult("child suite");
        suite.add(childSuite);
        List<PersistentTestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        assertTrue(children.get(0) == childSuite);
    }

    public void testAddCaseAlreadyExists()
    {
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 100, PASS, "test message");
        PersistentTestCaseResult childCase2 = new PersistentTestCaseResult("acase", 100, PASS, "test message");
        suite.add(childCase);
        suite.add(childCase2);
        
        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        assertTrue(children[0].isEquivalent(childCase));
    }

    public void testAddCaseLessSevereAlreadyExists()
    {
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 100, PASS, "test message");
        PersistentTestCaseResult childCase2 = new PersistentTestCaseResult("acase", 102, FAILURE, "failure message");
        suite.add(childCase);
        suite.add(childCase2);

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        assertFalse(children[0].isEquivalent(childCase));
        assertTrue(children[0].isEquivalent(childCase2));
    }

    public void testAddCaseMoreSevereAlreadyExists()
    {
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 102, FAILURE, "failure message");
        PersistentTestCaseResult childCase2 = new PersistentTestCaseResult("acase", 100, PASS, "test message");
        suite.add(childCase);
        suite.add(childCase2);

        PersistentTestCaseResult[] children = suite.getCases().toArray(new PersistentTestCaseResult[suite.getCases().size()]);
        assertEquals(1, children.length);
        assertTrue(children[0].isEquivalent(childCase));
        assertFalse(children[0].isEquivalent(childCase2));
    }

    public void testAddSuiteAlreadyExists()
    {
        PersistentTestSuiteResult childSuite = new PersistentTestSuiteResult("child suite");
        PersistentTestSuiteResult childSuite2 = new PersistentTestSuiteResult("child suite");
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 1002, PASS, null);
        PersistentTestCaseResult childCase2 = new PersistentTestCaseResult("acase2", 102, FAILURE, "failure message");
        childSuite.add(childCase);
        childSuite.add(childCase2);
        suite.add(childSuite);
        suite.add(childSuite2);

        List<PersistentTestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        PersistentTestSuiteResult suiteResult = children.get(0);
        assertEquals(2, suiteResult.getCases().size());
        assertTrue(suiteResult.getCase(childCase.getName()).isEquivalent(childCase));
        assertTrue(suiteResult.getCase(childCase2.getName()).isEquivalent(childCase2));
    }

    public void testAddSuiteAlreadyExistsOverlappingCase()
    {
        PersistentTestSuiteResult childSuite = new PersistentTestSuiteResult("child suite");
        PersistentTestSuiteResult childSuite2 = new PersistentTestSuiteResult("child suite");
        PersistentTestCaseResult childCase = new PersistentTestCaseResult("acase", 1002, PASS, null);
        PersistentTestCaseResult childCase2 = new PersistentTestCaseResult("acase", 102, FAILURE, "failure message");
        childSuite.add(childCase);
        childSuite.add(childCase2);
        suite.add(childSuite);
        suite.add(childSuite2);

        List<PersistentTestSuiteResult> children = suite.getSuites();
        assertEquals(1, children.size());
        PersistentTestSuiteResult suiteResult = children.get(0);
        assertEquals(1, suiteResult.getCases().size());
        PersistentTestCaseResult[] cases = suiteResult.getCases().toArray(new PersistentTestCaseResult[suiteResult.getCases().size()]);
        assertTrue(cases[0].isEquivalent(childCase2));
    }
}
