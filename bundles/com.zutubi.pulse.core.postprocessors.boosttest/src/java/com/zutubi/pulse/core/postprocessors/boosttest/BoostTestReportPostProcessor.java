package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Post-processor for extracting test reports from Boost.Test XML test logs. To
 * produce these logs run a Boost.Test executable with flags:
 * 
 * --output_format=XML --log_level=test_suite
 * 
 */
public class BoostTestReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final String ELEMENT_TEST_LOG = "TestLog";
    private static final String ELEMENT_TEST_SUITE = "TestSuite";
    private static final String ELEMENT_TEST_CASE = "TestCase";
    private static final String ELEMENT_TESTING_TIME = "TestingTime";
    private static final String ELEMENT_ERROR = "Error";
    private static final String ELEMENT_FATAL_ERROR = "FatalError";
    private static final String ELEMENT_EXCEPTION = "Exception";
    private static final String ELEMENT_MESSAGE = "Message";
    private static final String ELEMENT_INFO = "Info";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_LINE = "line";

    private static final Map<String, TestStatus> ELEMENT_TO_STATUS = new HashMap<String, TestStatus>();
    private static final Map<String, String> ELEMENT_TO_MESSAGE = new HashMap<String, String>();

    static
    {
        ELEMENT_TO_STATUS.put(ELEMENT_ERROR, TestStatus.FAILURE);
        ELEMENT_TO_STATUS.put(ELEMENT_FATAL_ERROR, TestStatus.FAILURE);
        ELEMENT_TO_STATUS.put(ELEMENT_EXCEPTION, TestStatus.ERROR);

        ELEMENT_TO_MESSAGE.put(ELEMENT_ERROR, "error");
        ELEMENT_TO_MESSAGE.put(ELEMENT_FATAL_ERROR, "fatal error");
        ELEMENT_TO_MESSAGE.put(ELEMENT_EXCEPTION, "uncaught exception");
        ELEMENT_TO_MESSAGE.put(ELEMENT_MESSAGE, "message");
        ELEMENT_TO_MESSAGE.put(ELEMENT_INFO, "info");
    }

    public BoostTestReportPostProcessor(BoostTestReportPostProcessorConfiguration config)
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
                    processSuites(reader, tests);
                }
            }
        });
    }

    private void processSuites(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartElement(ELEMENT_TEST_LOG, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (isElement(ELEMENT_TEST_SUITE, reader))
            {
                processSuite(reader, tests);
            }
            else
            {
                nextElement(reader);
            }
        }
        expectEndElement(ELEMENT_TEST_LOG, reader);
    }

    private void processSuite(XMLStreamReader reader, TestSuiteResult parentSuite) throws XMLStreamException
    {
        expectStartElement(ELEMENT_TEST_SUITE, reader);

        Map<String, String> attributes = getAttributes(reader);
        if (!attributes.containsKey(ATTRIBUTE_NAME))
        {
            // We can not process a suite without a name.  Skip to the next element.
            nextElement(reader);
            return;
        }

        TestSuiteResult suiteResult = new TestSuiteResult(attributes.get(ATTRIBUTE_NAME));
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (isElement(ELEMENT_TEST_SUITE, reader))
            {
                processSuite(reader, suiteResult);
            }
            else if (isElement(ELEMENT_TEST_CASE, reader))
            {
                processCase(reader, suiteResult);
            }
            else
            {
                nextElement(reader);
            }
        }

        suiteResult.setDuration(getTotalDuration(suiteResult));
        parentSuite.addSuite(suiteResult);

        expectEndElement(ELEMENT_TEST_SUITE, reader);
        reader.nextTag();
    }

    private void processCase(XMLStreamReader reader, TestSuiteResult parentSuite) throws XMLStreamException
    {
        expectStartElement(ELEMENT_TEST_CASE, reader);

        Map<String, String> attributes = getAttributes(reader);
        if (!attributes.containsKey(ATTRIBUTE_NAME))
        {
            nextElement(reader);
            return;
        }
        reader.nextTag();

        long duration = TestResult.DURATION_UNKNOWN;
        TestStatus status = TestStatus.PASS;
        BoostTestReportPostProcessorConfiguration config = (BoostTestReportPostProcessorConfiguration) getConfig();
        StringBuilder builder = new StringBuilder();

        while (reader.isStartElement())
        {
            if (isElement(ELEMENT_TESTING_TIME, reader))
            {
                try
                {
                    duration = (long) (Double.parseDouble(getElementText(reader, "").trim()) / 1000);
                }
                catch (NumberFormatException e)
                {
                    // the default value will have to do.
                }
                reader.nextTag();
            }
            else
            {
                String name = reader.getLocalName();

                TestStatus brokenStatus = ELEMENT_TO_STATUS.get(name);
                if (brokenStatus == null)
                {
                    if (config.isProcessMessages() && name.equals(ELEMENT_MESSAGE) || config.isProcessInfo() && name.equals(ELEMENT_INFO))
                    {
                        appendMessage(reader, builder);
                        expectEndElement(name, reader);
                        reader.nextTag();
                    }
                    else
                    {
                        nextElement(reader);
                    }
                }
                else
                {
                    if (brokenStatus.compareTo(status) > 0)
                    {
                        status = brokenStatus;
                    }

                    appendMessage(reader, builder);
                    expectEndElement(name, reader);
                    reader.nextTag();
                }
            }
        }

        parentSuite.addCase(new TestCaseResult(attributes.get(ATTRIBUTE_NAME), duration, status, builder.length() > 0 ? builder.toString() : null));

        expectEndElement(ELEMENT_TEST_CASE, reader);
        reader.nextTag();
    }

    private void appendMessage(XMLStreamReader reader, StringBuilder builder) throws XMLStreamException
    {
        String message = ELEMENT_TO_MESSAGE.get(reader.getLocalName());
        Map<String, String> attributes = getAttributes(reader);
        String file = attributes.get(ATTRIBUTE_FILE);
        if (file != null)
        {
            message += ": " + file;

            String line = attributes.get(ATTRIBUTE_LINE);
            if (line != null)
            {
                message += ":" + line;
            }
        }

        message += ": " + getElementText(reader, "").trim();
        if (builder.length() > 0)
        {
            builder.append("\n");
        }
        builder.append(message);
    }

    private long getTotalDuration(TestSuiteResult suiteResult)
    {
        long totalDuration = 0;
        for (TestSuiteResult childSuite: suiteResult.getSuites())
        {
            totalDuration += childSuite.getDuration();
        }

        for (TestCaseResult childCase: suiteResult.getCases())
        {
            totalDuration += childCase.getDuration();
        }

        return totalDuration;
    }
}
