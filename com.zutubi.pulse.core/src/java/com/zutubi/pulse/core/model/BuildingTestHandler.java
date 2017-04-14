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

import nu.xom.Element;

import java.util.Stack;

/**
 */
public class BuildingTestHandler implements TestHandler
{
    private PersistentTestSuiteResult top;
    private Stack<PersistentTestSuiteResult> suites = new Stack<PersistentTestSuiteResult>();

    public PersistentTestSuiteResult getTop()
    {
        return top;
    }

    public void startSuite(PersistentTestSuiteResult suiteResult)
    {
        if(suites.size() > 0)
        {
            suites.peek().add(suiteResult);
        }
        else
        {
            top = suiteResult;
        }

        suites.push(suiteResult);
    }

    public boolean endSuite()
    {
        suites.pop();
        return false;
    }

    public void handleCase(PersistentTestCaseResult caseResult, Element element)
    {
        suites.peek().add(caseResult);
    }
}
