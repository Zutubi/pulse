package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Post-processor for CUnit version 2 (and compatible) XML reports.  See:
 * http://cunit.sourceforge.net/.
 */
public class CUnitReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_REPORT = "CUNIT_TEST_RUN_REPORT";
    private static final String ELEMENT_RESULT_LISTING = "CUNIT_RESULT_LISTING";
    private static final String ELEMENT_RUN_SUITE = "CUNIT_RUN_SUITE";
    private static final String ELEMENT_RUN_SUITE_SUCCESS = "CUNIT_RUN_SUITE_SUCCESS";
    private static final String ELEMENT_RUN_SUITE_FAILURE = "CUNIT_RUN_SUITE_FAILURE";
    private static final String ELEMENT_SUITE_NAME = "SUITE_NAME";
    private static final String ELEMENT_SUITE_FAILURE_REASON = "FAILURE_REASON";
    private static final String ELEMENT_RUN_TEST = "CUNIT_RUN_TEST_RECORD";
    private static final String ELEMENT_RUN_TEST_SUCCESS = "CUNIT_RUN_TEST_SUCCESS";
    private static final String ELEMENT_RUN_TEST_FAILURE = "CUNIT_RUN_TEST_FAILURE";
    private static final String ELEMENT_TEST_NAME = "TEST_NAME";
    private static final String ELEMENT_TEST_FILE_NAME = "FILE_NAME";
    private static final String ELEMENT_TEST_LINE_NUMBER = "LINE_NUMBER";
    private static final String ELEMENT_TEST_CONDITION = "CONDITION";

    public CUnitReportPostProcessor(CUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_REPORT, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_RESULT_LISTING))
        {
            handleResultListing(tests, reader);
        }

        expectEndTag(ELEMENT_REPORT, reader);
    }

    private void handleResultListing(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RESULT_LISTING, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_RUN_SUITE))
        {
            handleRunSuite(tests, reader);
        }

        expectEndTag(ELEMENT_RESULT_LISTING, reader);
        nextTagOrEnd(reader);
    }

    private void handleRunSuite(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_SUITE, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_RUN_SUITE_SUCCESS, ELEMENT_RUN_SUITE_FAILURE))
        {
            if (isElement(ELEMENT_RUN_SUITE_SUCCESS, reader))
            {
                handleSuiteSuccess(tests, reader);
            }
            else if (isElement(ELEMENT_RUN_SUITE_FAILURE, reader))
            {
                handleSuiteFailure(tests, reader);
            }
        }

        expectEndTag(ELEMENT_RUN_SUITE, reader);
        nextTagOrEnd(reader);
    }

    private void handleSuiteSuccess(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_SUITE_SUCCESS, reader);
        nextTagOrEnd(reader);

        if (nextSiblingTag(reader, ELEMENT_SUITE_NAME))
        {
            expectStartTag(ELEMENT_SUITE_NAME, reader);
            String suiteName = reader.getElementText().trim();
            expectEndTag(ELEMENT_SUITE_NAME, reader);
            nextTagOrEnd(reader);

            TestSuiteResult suite = new TestSuiteResult(suiteName);
            tests.addSuite(suite);

            while (nextSiblingTag(reader, ELEMENT_RUN_TEST))
            {
                handleTestRecord(suite, reader);
            }
        }
        else
        {
            // No suite name means skipping the entire suite.
            while (reader.isStartElement())
            {
                nextElement(reader);
            }
        }

        expectEndTag(ELEMENT_RUN_SUITE_SUCCESS, reader);
        nextTagOrEnd(reader);
    }

    private void handleTestRecord(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_TEST, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_RUN_TEST_SUCCESS, ELEMENT_RUN_TEST_FAILURE))
        {
            if (isElement(ELEMENT_RUN_TEST_SUCCESS, reader))
            {
                handleTestSuccess(suite, reader);
            }
            else if (isElement(ELEMENT_RUN_TEST_FAILURE, reader))
            {
                handleTestFailure(suite, reader);
            }
        }

        expectEndTag(ELEMENT_RUN_TEST, reader);
        nextTagOrEnd(reader);
    }

    private void handleTestSuccess(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_TEST_SUCCESS, reader);
        nextTagOrEnd(reader);

        Map<String, String> elements = readElements(reader);
        if (elements.containsKey(ELEMENT_TEST_NAME))
        {
            String name = elements.get(ELEMENT_TEST_NAME).trim();
            suite.addCase(new TestCaseResult(name));
        }

        expectEndTag(ELEMENT_RUN_TEST_SUCCESS, reader);
        nextTagOrEnd(reader);
    }

    private void handleTestFailure(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_TEST_FAILURE, reader);
        nextTagOrEnd(reader);

        Map<String, String> elements = readElements(reader);

        if (elements.containsKey(ELEMENT_TEST_NAME))
        {
            String name = getTrimmedValue(elements, ELEMENT_TEST_NAME, "");
            String filename = getTrimmedValue(elements, ELEMENT_TEST_FILE_NAME, "<unknown>");
            String linenumber = getTrimmedValue(elements, ELEMENT_TEST_LINE_NUMBER, "-1");
            String condition = getTrimmedValue(elements, ELEMENT_TEST_CONDITION, "");

            String message = String.format("%s: %s: %s", filename, linenumber, condition);

            TestCaseResult caseResult = suite.findCase(name);
            if (caseResult == null)
            {
                suite.addCase(new TestCaseResult(name, TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, message));
            }
            else
            {
                // Another failed assertion already - tag this one on.
                caseResult.setMessage(caseResult.getMessage() + '\n' + message);
            }
        }

        expectEndTag(ELEMENT_RUN_TEST_FAILURE, reader);
        nextTagOrEnd(reader);
    }

    private void handleSuiteFailure(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartTag(ELEMENT_RUN_SUITE_FAILURE, reader);
        nextTagOrEnd(reader);

        if (nextSiblingTag(reader, ELEMENT_SUITE_NAME))
        {
            expectStartTag(ELEMENT_SUITE_NAME, reader);
            String name = reader.getElementText().trim();
            expectEndTag(ELEMENT_SUITE_NAME, reader);
            nextTagOrEnd(reader);

            String failureReason = "unknown";
            if (nextSiblingTag(reader, ELEMENT_SUITE_FAILURE_REASON))
            {
                expectStartTag(ELEMENT_SUITE_FAILURE_REASON, reader);
                failureReason = reader.getElementText().trim();
                expectEndTag(ELEMENT_SUITE_FAILURE_REASON, reader);
                nextTagOrEnd(reader);
            }

            TestSuiteResult suite = new TestSuiteResult(name);
            suite.addCase(new TestCaseResult("Suite Failure Notification", TestResult.DURATION_UNKNOWN, TestStatus.ERROR, failureReason));
            tests.addSuite(suite);
        }
        else
        {
            // no suite name, so we skip it.
            while (reader.isStartElement())
            {
                nextElement(reader);
            }
        }

        expectEndTag(ELEMENT_RUN_SUITE_FAILURE, reader);
        nextTagOrEnd(reader);
    }

    private String getTrimmedValue(Map<String, String> data, String name, String defaultValue)
    {
        return data.containsKey(name) ? (data.get(name)).trim() : defaultValue;
    }

    protected XMLInputFactory createFactory()
    {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.supportDTD", false);
        return inputFactory;
    }
}
