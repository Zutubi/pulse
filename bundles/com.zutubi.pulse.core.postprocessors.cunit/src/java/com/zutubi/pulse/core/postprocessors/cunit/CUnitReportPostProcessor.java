package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.Map;

/**
 * Post-processor for CUnit version 2 (and compatible) XML reports.  See:
 * http://cunit.sourceforge.net/.
 */
public class CUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
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

    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        process(file, ppContext, tests, new XMLStreamCallback()
        {
            public void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
            {
                if (nextElement(reader))
                {
                    handleRunReport(tests, reader);
                }
            }
        });
    }

    private void handleRunReport(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_REPORT, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (reader.getLocalName().equals(ELEMENT_RESULT_LISTING))
            {
                handleResultListing(tests, reader);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndElement(ELEMENT_REPORT, reader);
        reader.nextTag();
    }

    private void handleResultListing(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RESULT_LISTING, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (reader.getLocalName().equals(ELEMENT_RUN_SUITE))
            {
                handleRunSuite(tests, reader);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndElement(ELEMENT_RESULT_LISTING, reader);
        reader.nextTag();
    }

    private void handleRunSuite(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_SUITE, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (reader.getLocalName().equals(ELEMENT_RUN_SUITE_SUCCESS))
            {
                handleSuiteSuccess(tests, reader);
            }
            else if (reader.getLocalName().equals(ELEMENT_RUN_SUITE_FAILURE))
            {
                handleSuiteFailure(tests, reader);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndElement(ELEMENT_RUN_SUITE, reader);
        reader.nextTag();
    }

    private void handleSuiteSuccess(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_SUITE_SUCCESS, reader);
        reader.nextTag();

        expectStartElement(ELEMENT_SUITE_NAME, reader);
        String suiteName = getElementText(reader, "").trim();
        expectEndElement(ELEMENT_SUITE_NAME, reader);
        reader.nextTag();

        TestSuiteResult suite = new TestSuiteResult(suiteName);
        tests.addSuite(suite);

        while (reader.isStartElement())
        {
            if (reader.getLocalName().equals(ELEMENT_RUN_TEST))
            {
                handleTestRecord(suite, reader);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndElement(ELEMENT_RUN_SUITE_SUCCESS, reader);
        reader.nextTag();
    }

    private void handleTestRecord(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_TEST, reader);
        reader.nextTag(); 

        if (reader.getLocalName().equals(ELEMENT_RUN_TEST_SUCCESS))
        {
            handleTestSuccess(suite, reader);
        }
        else if (reader.getLocalName().equals(ELEMENT_RUN_TEST_FAILURE))
        {
            handleTestFailure(suite, reader);
        }

        expectEndElement(ELEMENT_RUN_TEST, reader);
        reader.nextTag();
    }

    private void handleTestSuccess(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_TEST_SUCCESS, reader);
        reader.nextTag();
        
        expectStartElement(ELEMENT_TEST_NAME, reader);
        String name = getElementText(reader, "").trim();
        suite.addCase(new TestCaseResult(name));
        
        expectEndElement(ELEMENT_TEST_NAME, reader);
        reader.nextTag();
        
        expectEndElement(ELEMENT_RUN_TEST_SUCCESS, reader);
        reader.nextTag();
    }

    private void handleTestFailure(TestSuiteResult suite, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_TEST_FAILURE, reader);
        reader.nextTag();

        Map<String, String> elements = readElements(reader);

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

        expectEndElement(ELEMENT_RUN_TEST_FAILURE, reader);
        reader.nextTag();
    }

    private void handleSuiteFailure(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        expectStartElement(ELEMENT_RUN_SUITE_FAILURE, reader);
        reader.nextTag();

        expectStartElement(ELEMENT_SUITE_NAME, reader);
        String name = getElementText(reader, "").trim();
        expectEndElement(ELEMENT_SUITE_NAME, reader);
        reader.nextTag();

        expectStartElement(ELEMENT_SUITE_FAILURE_REASON, reader);
        String failureReason = getElementText(reader, "").trim();
        expectEndElement(ELEMENT_SUITE_FAILURE_REASON, reader);
        reader.nextTag();

        TestSuiteResult suite = new TestSuiteResult(name);
        suite.addCase(new TestCaseResult("Suite Failure Notification", TestResult.DURATION_UNKNOWN, TestStatus.ERROR, failureReason));
        tests.addSuite(suite);

        expectEndElement(ELEMENT_RUN_SUITE_FAILURE, reader);
        reader.nextTag();
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
