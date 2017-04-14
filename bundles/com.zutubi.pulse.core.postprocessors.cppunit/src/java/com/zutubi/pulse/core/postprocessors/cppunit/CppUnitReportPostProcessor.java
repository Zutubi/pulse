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

package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Post-processor for cppunit (and compatible) XML reports.  See:
 * http://sourceforge.net/apps/mediawiki/cppunit/index.php
 */
public class CppUnitReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_TEST_RUN = "TestRun";
    private static final String ELEMENT_SUCCESSFUL_TESTS = "SuccessfulTests";
    private static final String ELEMENT_TEST = "Test";
    private static final String ELEMENT_FAILED_TESTS = "FailedTests";
    private static final String ELEMENT_FAILED_TEST = "FailedTest";
    private static final String ELEMENT_NAME = "Name";
    private static final String ELEMENT_FAILURE_TYPE = "FailureType";
    private static final String ELEMENT_LOCATION = "Location";
    private static final String ELEMENT_FILE = "File";
    private static final String ELEMENT_LINE = "Line";
    private static final String ELEMENT_MESSAGE = "Message";

    private static final String FAILURE_TYPE_ERROR = "Error";

    public CppUnitReportPostProcessor(CppUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST_RUN, reader);
        nextTagOrEnd(reader);

        Map<String, TestSuiteResult> suites = new TreeMap<String, TestSuiteResult>();

        while (nextSiblingTag(reader, ELEMENT_FAILED_TESTS, ELEMENT_SUCCESSFUL_TESTS))
        {
            if (reader.getLocalName().equals(ELEMENT_FAILED_TESTS))
            {
                handleFailedTests(suites, reader);
            }
            else if (reader.getLocalName().equals(ELEMENT_SUCCESSFUL_TESTS))
            {
                handleSuccessfulTests(suites, reader);
            }
        }

        addSuites(tests, suites);

        expectEndTag(ELEMENT_TEST_RUN, reader);
    }

    private void handleSuccessfulTests(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_SUCCESSFUL_TESTS, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_TEST))
        {
            handleSuccessfulTest(suites, reader);
        }

        expectEndTag(ELEMENT_SUCCESSFUL_TESTS, reader);
        nextTagOrEnd(reader);
    }

    private void handleSuccessfulTest(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST, reader);
        nextTagOrEnd(reader);

        String[] name = new String[0];
        while (nextSiblingTag(reader, ELEMENT_NAME))
        {
            name = handleGetTestName(reader);
        }

        if (name.length != 0)
        {
            TestSuiteResult suite = getSuite(name[0], suites);
            TestCaseResult result = new TestCaseResult(name[1]);
            suite.addCase(result);
        }

        expectEndTag(ELEMENT_TEST, reader);
        nextTagOrEnd(reader);
    }

    private void handleFailedTests(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_FAILED_TESTS, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_FAILED_TEST))
        {
            handleFailedTest(suites, reader);
        }

        expectEndTag(ELEMENT_FAILED_TESTS, reader);
        nextTagOrEnd(reader);
    }

    private void handleFailedTest(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_FAILED_TEST, reader);
        nextTagOrEnd(reader);

        String[] name = new String[0];
        Map<String, String> location = null;
        TestStatus status = null;
        String messageText = null;

        while (nextSiblingTag(reader, ELEMENT_NAME, ELEMENT_FAILURE_TYPE, ELEMENT_LOCATION, ELEMENT_MESSAGE))
        {
            if (isElement(ELEMENT_NAME, reader))
            {
                name = handleGetTestName(reader);
            }
            else if (isElement(ELEMENT_FAILURE_TYPE, reader))
            {
                status = handleGetFailedTestStatus(reader);
            }
            else if (isElement(ELEMENT_LOCATION, reader))
            {
                location = handleGetLocation(reader);
            }
            else if (isElement(ELEMENT_MESSAGE, reader))
            {
                messageText = handleGetMessage(reader);
            }
        }

        if (name.length != 0)
        {
            String message = formatMessage(location, messageText);

            TestSuiteResult suite = getSuite(name[0], suites);
            TestCaseResult result = new TestCaseResult(name[1], TestResult.DURATION_UNKNOWN, status, message);
            suite.addCase(result);
        }

        expectEndTag(ELEMENT_FAILED_TEST, reader);
        nextTagOrEnd(reader);
    }

    private Map<String, String> handleGetLocation(XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_LOCATION, reader);
        nextTagOrEnd(reader);

        Map<String, String> location = readElements(reader);

        expectEndTag(ELEMENT_LOCATION, reader);
        nextTagOrEnd(reader);

        return location;
    }

    private String handleGetMessage(XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_MESSAGE, reader);
        String messageText = reader.getElementText().trim();

        expectEndTag(ELEMENT_MESSAGE, reader);
        nextTagOrEnd(reader);

        return messageText;
    }

    private String formatMessage(Map<String, String> location, String messageText)
    {
        String message = "";
        if (location != null)
        {
            String locationText = "At";
            if (location.containsKey(ELEMENT_FILE))
            {
                locationText += " file " + location.get(ELEMENT_FILE).trim();
            }

            if (location.containsKey(ELEMENT_LINE))
            {
                locationText += " line " + location.get(ELEMENT_LINE).trim();
            }
            message += locationText + "\n";
        }

        if (messageText != null)
        {
            message += messageText.trim();
        }
        return message;
    }

    private TestStatus handleGetFailedTestStatus(XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_FAILURE_TYPE, reader);
        String typeText = reader.getElementText().trim();
        TestStatus status = typeText.equals(FAILURE_TYPE_ERROR) ? TestStatus.ERROR : TestStatus.FAILURE;
        expectEndTag(ELEMENT_FAILURE_TYPE, reader);
        nextTagOrEnd(reader);

        return status;
    }

    private String[] handleGetTestName(XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_NAME, reader);
        String nameText = reader.getElementText().trim();
        String[] bits = nameText.split("::", 2);
        String[] name = (bits.length == 1) ? new String[]{ "[unknown]", bits[0] } : bits;
        expectEndTag(ELEMENT_NAME, reader);
        nextTagOrEnd(reader);

        return name;
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
