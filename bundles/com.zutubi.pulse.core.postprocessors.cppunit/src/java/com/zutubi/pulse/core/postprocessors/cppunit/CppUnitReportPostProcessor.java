package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Post-processor for cppunit (and compatible) XML reports.  See:
 * http://sourceforge.net/apps/mediawiki/cppunit/index.php
 */
public class CppUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
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

    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        process(file, ppContext, tests, new XMLStreamCallback()
        {
            public void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
            {
                if (nextElement(reader))
                {
                    handleTestRun(tests, reader);
                }
            }
        });
    }

    private void handleTestRun(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_TEST_RUN, reader);
        reader.nextTag();

        Map<String, TestSuiteResult> suites = new TreeMap<String, TestSuiteResult>();

        while (reader.isStartElement())
        {
            if (reader.getLocalName().equals(ELEMENT_FAILED_TESTS))
            {
                handleFailedTests(suites, reader);
            }
            else if (reader.getLocalName().equals(ELEMENT_SUCCESSFUL_TESTS))
            {
                handleSuccessfulTests(suites, reader);
            }
            else
            {
                nextElement(reader);
            }
        }

        addSuites(tests, suites);

        expectEndElement(ELEMENT_TEST_RUN, reader);
    }

    private void handleSuccessfulTests(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_SUCCESSFUL_TESTS, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            handleSuccessfulTest(suites, reader);
        }

        expectEndElement(ELEMENT_SUCCESSFUL_TESTS, reader);
        reader.nextTag();
    }

    private void handleSuccessfulTest(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_TEST, reader);
        reader.nextTag();

        String[] name = handleGetTestName(reader);

        TestSuiteResult suite = getSuite(name[0], suites);
        TestCaseResult result = new TestCaseResult(name[1]);
        suite.addCase(result);

        expectEndElement(ELEMENT_TEST, reader);
        reader.nextTag();
    }

    private void handleFailedTests(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_FAILED_TESTS, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            handleFailedTest(suites, reader);
        }

        expectEndElement(ELEMENT_FAILED_TESTS, reader);
        reader.nextTag();
    }

    private void handleFailedTest(Map<String, TestSuiteResult> suites, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_FAILED_TEST, reader);
        reader.nextTag();

        // warning: this assumes the ordering of the tags in the xml report.  Is this a reasonable
        // assumption to be made?
        String[] name = handleGetTestName(reader);
        TestStatus status = handleGetFailedTestStatus(reader);
        String message = handleGetFailedTestMessage(reader);

        TestSuiteResult suite = getSuite(name[0], suites);
        TestCaseResult result = new TestCaseResult(name[1], TestResult.DURATION_UNKNOWN, status, message);
        suite.addCase(result);

        expectEndElement(ELEMENT_FAILED_TEST, reader);
        reader.nextTag();
    }

    private String handleGetFailedTestMessage(XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> location = null;
        if (reader.getLocalName().equals(ELEMENT_LOCATION))
        {
            expectStartElement(ELEMENT_LOCATION, reader);
            reader.nextTag();

            location = readElements(reader);

            expectEndElement(ELEMENT_LOCATION, reader);
            reader.nextTag();
        }

        String messageText = null;
        if (reader.getLocalName().equals(ELEMENT_MESSAGE))
        {
            expectStartElement(ELEMENT_MESSAGE, reader);
            messageText = getElementText(reader, "").trim();

            expectEndElement(ELEMENT_MESSAGE, reader);
            reader.nextTag();
        }

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
        expectStartElement(ELEMENT_FAILURE_TYPE, reader);
        String typeText = getElementText(reader, "").trim();
        TestStatus status = typeText.equals(FAILURE_TYPE_ERROR) ? TestStatus.ERROR : TestStatus.FAILURE;
        expectEndElement(ELEMENT_FAILURE_TYPE, reader);
        reader.nextTag();

        return status;
    }

    private String[] handleGetTestName(XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_NAME, reader);
        String nameText = getElementText(reader, "").trim();
        String[] bits = nameText.split("::", 2);
        String[] name = (bits.length == 1) ? new String[]{ "[unknown]", bits[0] } : bits;
        expectEndElement(ELEMENT_NAME, reader);
        reader.nextTag();

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
