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

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.api.XMLStreamUtils;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Post-processor for UnitTest++ (and compatible) XML reports.  See:
 * http://unittest-cpp.sourceforge.net/
 */
public class UnitTestPlusPlusReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_RESULTS = "unittest-results";
    private static final String ELEMENT_TEST = "test";
    private static final String ELEMENT_FAILURE = "failure";

    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TIME = "time";
    private static final String ATTRIBUTE_SUITE = "suite";

    public UnitTestPlusPlusReportPostProcessor(UnitTestPlusPlusReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RESULTS, reader);
        nextTagOrEnd(reader);

        Map<String, TestSuiteResult> suites = new TreeMap<String, TestSuiteResult>();

        while (XMLStreamUtils.nextSiblingTag(reader, ELEMENT_TEST))
        {
            processTest(reader, suites);
        }

        addSuites(tests, suites);

        expectEndTag(ELEMENT_RESULTS, reader);
    }

    private void processTest(XMLStreamReader reader, Map<String, TestSuiteResult> suites) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST, reader);
        Map<String, String> attributes = getAttributes(reader);

        String suite = attributes.get(ATTRIBUTE_SUITE);
        String name = attributes.get(ATTRIBUTE_NAME);
        long duration = getDuration(attributes);

        if(suite != null && name != null)
        {
            nextTagOrEnd(reader);

            TestSuiteResult suiteResult = getSuite(suite, suites);

            TestCaseResult caseResult = null;
            if (XMLStreamUtils.nextSiblingTag(reader, ELEMENT_FAILURE))
            {
                attributes = getAttributes(reader);
                caseResult = new TestCaseResult(name, duration, TestStatus.FAILURE, attributes.get(ATTRIBUTE_MESSAGE));
                while (reader.isStartElement())
                {
                    nextElement(reader);
                }
            }

            if (caseResult == null)
            {
                caseResult = new TestCaseResult(name, duration, TestStatus.PASS);
            }

            suiteResult.addCase(caseResult);
        }
        else
        {
            skipElement(reader);
        }

        expectEndTag(ELEMENT_TEST, reader);
        nextTagOrEnd(reader);
    }

    private long getDuration(Map<String, String> attributes)
    {
        String value = attributes.get(ATTRIBUTE_TIME);
        if(value != null)
        {
            try
            {
                return (long) (Double.parseDouble(value) * 1000);
            }
            catch (NumberFormatException e)
            {
                // Fall through
            }
        }

        return TestResult.DURATION_UNKNOWN;
    }

    private void addSuites(TestSuiteResult tests, Map<String, TestSuiteResult> suites)
    {
        for(TestSuiteResult suite: suites.values())
        {
            tests.addSuite(suite);
        }
    }

    private TestSuiteResult getSuite(String name, Map<String, TestSuiteResult> suites)
    {
        if(suites.containsKey(name))
        {
            return suites.get(name);
        }
        else
        {
            TestSuiteResult suite = new TestSuiteResult(name);
            suites.put(name, suite);
            return suite;
        }
    }
}
