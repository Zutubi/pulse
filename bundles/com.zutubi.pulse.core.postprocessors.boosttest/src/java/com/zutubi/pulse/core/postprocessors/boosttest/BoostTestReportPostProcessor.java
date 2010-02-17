package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.*;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.pulse.core.util.api.XMLStreamUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Post-processor for extracting test reports from Boost.Test XML test logs. To
 * produce these logs run a Boost.Test executable with flags:
 * 
 * --output_format=XML --log_level=test_suite
 * 
 */
public class BoostTestReportPostProcessor extends StAXTestReportPostProcessorSupport
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

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        while (!(reader.isStartElement() && isElement(ELEMENT_TEST_LOG, reader)))
        {
            if (nextTagOrEnd(reader) == XMLStreamReader.END_DOCUMENT)
            {
                return;
            }
        }

        nextTagOrEnd(reader);
        while (nextSiblingTag(reader, ELEMENT_TEST_SUITE))
        {
            processSuite(reader, tests);
        }
        expectEndTag(ELEMENT_TEST_LOG, reader);
    }

    private void processSuite(XMLStreamReader reader, TestSuiteResult parentSuite) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST_SUITE, reader);

        Map<String, String> attributes = getAttributes(reader);
        if (!attributes.containsKey(ATTRIBUTE_NAME))
        {
            // We can not process a suite without a name.  Skip to the next element.
            nextElement(reader);
            return;
        }

        TestSuiteResult suiteResult = new TestSuiteResult(attributes.get(ATTRIBUTE_NAME));
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, ELEMENT_TEST_SUITE, ELEMENT_TEST_CASE))
        {
            if (isElement(ELEMENT_TEST_SUITE, reader))
            {
                processSuite(reader, suiteResult);
            }
            else if (isElement(ELEMENT_TEST_CASE, reader))
            {
                processCase(reader, suiteResult);
            }
        }

        parentSuite.addSuite(suiteResult);

        expectEndTag(ELEMENT_TEST_SUITE, reader);
        nextTagOrEnd(reader);
    }

    private void processCase(XMLStreamReader reader, TestSuiteResult parentSuite) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST_CASE, reader);

        Map<String, String> attributes = getAttributes(reader);
        if (!attributes.containsKey(ATTRIBUTE_NAME))
        {
            nextElement(reader);
            return;
        }
        nextTagOrEnd(reader);

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
                    duration = (long) (Double.parseDouble(reader.getElementText().trim()) / 1000);
                }
                catch (NumberFormatException e)
                {
                    // the default value will have to do.
                }
                nextTagOrEnd(reader);
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
                        expectEndTag(name, reader);
                        nextTagOrEnd(reader);
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
                    expectEndTag(name, reader);
                    nextTagOrEnd(reader);
                }
            }
        }

        parentSuite.addCase(new TestCaseResult(attributes.get(ATTRIBUTE_NAME), duration, status, builder.length() > 0 ? builder.toString() : null));

        expectEndTag(ELEMENT_TEST_CASE, reader);
        nextTagOrEnd(reader);
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

        message += ": " + XMLStreamUtils.getElementText(reader).trim();
        if (builder.indexOf(message) == -1)
        {
            if (builder.length() > 0)
            {
                builder.append("\n");
            }
            builder.append(message);
        }
    }
}
