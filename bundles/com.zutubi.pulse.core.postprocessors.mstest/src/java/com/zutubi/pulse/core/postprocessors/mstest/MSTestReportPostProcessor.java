package com.zutubi.pulse.core.postprocessors.mstest;

import com.zutubi.pulse.core.postprocessors.api.StAXTestReportPostProcessorSupport;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.*;

/**
 * Post-processor for MSTest TRX reports.
 */
public class MSTestReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final Logger LOG = Logger.getLogger(MSTestReportPostProcessor.class);

    private static final String ELEMENT_ROOT        = "TestRun";
    private static final String ELEMENT_DEFINITIONS = "TestDefinitions";
    private static final String ELEMENT_UNIT_TEST   = "UnitTest";
    private static final String ELEMENT_TEST_METHOD = "TestMethod";
    private static final String ELEMENT_RESULTS     = "Results";
    private static final String ELEMENT_TEST_RESULT = "UnitTestResult";
    private static final String ELEMENT_OUTPUT      = "Output";
    private static final String ELEMENT_ERROR_INFO  = "ErrorInfo";
    private static final String ELEMENT_MESSAGE     = "Message";
    private static final String ELEMENT_STACK_TRACE = "StackTrace";

    private static final String ATTRIBUTE_ID         = "id";
    private static final String ATTRIBUTE_CLASS_NAME = "className";
    private static final String ATTRIBUTE_TEST_ID    = "testId";
    private static final String ATTRIBUTE_TEST_NAME  = "testName";
    private static final String ATTRIBUTE_DURATION   = "duration";
    private static final String ATTRIBUTE_OUTCOME    = "outcome";

    private enum Outcome
    {
        Passed(TestStatus.PASS, null),
        Failed(TestStatus.FAILURE, null),
        Inconclusive(TestStatus.PASS, "Test result is inconclusive"),
        Timeout(TestStatus.ERROR, "Test timed out"),
        Aborted(TestStatus.ERROR, "Test aborted"),
        Blocked(TestStatus.SKIPPED, "Test blocked"),
        NotExecuted(TestStatus.SKIPPED, "Test not executed"),
        Warning(TestStatus.PASS, "Warnings reported"),
        Error(TestStatus.ERROR, null),
        ;

        private TestStatus status;
        private String defaultMessage;

        Outcome(TestStatus status, String defaultMessage)
        {
            this.status = status;
            this.defaultMessage = defaultMessage;
        }

        public TestStatus getStatus()
        {
            return status;
        }

        public String getDefaultMessage()
        {
            return defaultMessage;
        }
    }

    public MSTestReportPostProcessor(MSTestReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_ROOT, reader);
        nextTagOrEnd(reader);
        Map<String, String> idToSuite = new HashMap<String, String>();
        Map<String, TestCaseResult> idToCase = new LinkedHashMap<String, TestCaseResult>();

        // Definitions tell us the suite each case belongs to, but may appear after the cases in
        // the XML.  So we collect all definition and case information first.
        while (nextSiblingTag(reader, ELEMENT_DEFINITIONS, ELEMENT_RESULTS))
        {
            if (isElement(ELEMENT_DEFINITIONS, reader))
            {
                nextTagOrEnd(reader);
                while (nextSiblingTag(reader, ELEMENT_UNIT_TEST))
                {
                    processDefinition(reader, idToSuite);
                }

                expectEndTag(ELEMENT_DEFINITIONS, reader);
                nextTagOrEnd(reader);
            }
            else
            {
                nextTagOrEnd(reader);
                while (nextSiblingTag(reader, ELEMENT_TEST_RESULT))
                {
                    processResult(reader, idToCase);
                }

                expectEndTag(ELEMENT_RESULTS, reader);
                nextTagOrEnd(reader);
            }
        }

        // And now we can add suites to cases and to our parent case.
        for (Map.Entry<String, TestCaseResult> entry : idToCase.entrySet())
        {
            String suiteName = idToSuite.get(entry.getKey());
            if (suiteName != null)
            {
                TestSuiteResult suite = addSuite(tests, suiteName);
                suite.addCase(entry.getValue());
            }
        }

        expectEndTag(ELEMENT_ROOT, reader);
    }

    private void processDefinition(XMLStreamReader reader, Map<String, String> idToSuite) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        nextTagOrEnd(reader);

        String id = attributes.get(ATTRIBUTE_ID);
        if (StringUtils.stringSet(id))
        {
            if (nextSiblingTag(reader, ELEMENT_TEST_METHOD))
            {
                attributes = getAttributes(reader);
                String className = attributes.get(ATTRIBUTE_CLASS_NAME);
                if (className != null)
                {
                    idToSuite.put(id, getSuiteName(className));
                }

                skipElement(reader);
                expectEndTag(ELEMENT_TEST_METHOD, reader);
                nextTagOrEnd(reader);
            }
        }

        while (reader.isStartElement())
        {
            nextElement(reader);
        }

        expectEndTag(ELEMENT_UNIT_TEST, reader);
        nextTagOrEnd(reader);
    }

    private String getSuiteName(String className)
    {
        String[] pieces = className.split(",");
        return pieces[0].trim();
    }

    private TestSuiteResult addSuite(TestSuiteResult parentSuite, String name)
    {
        TestSuiteResult suite = parentSuite.findSuite(name);
        if (suite == null)
        {
            suite = new TestSuiteResult(name);
            parentSuite.addSuite(suite);
        }

        return suite;
    }

    private void processResult(XMLStreamReader reader, Map<String, TestCaseResult> idToCase) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        String id = attributes.get(ATTRIBUTE_TEST_ID);
        String name = attributes.get(ATTRIBUTE_TEST_NAME);
        if (!StringUtils.stringSet(id) || !StringUtils.stringSet(name))
        {
            nextElement(reader);
            return;
        }

        Outcome outcome = getOutcome(name, attributes);
        if (outcome == null)
        {
            nextElement(reader);
            return;
        }
        
        nextTagOrEnd(reader);
        
        TestCaseResult caseResult = new TestCaseResult(name, getDuration(attributes), outcome.getStatus());
        String defaultMessage = outcome.getDefaultMessage();
        String definedMessage = null;
        while (nextSiblingTag(reader, ELEMENT_OUTPUT))
        {
            definedMessage = getMessage(reader);
        }

        String message = null;
        if (StringUtils.stringSet(defaultMessage))
        {
            message = defaultMessage + "\n\n";
        }

        if (StringUtils.stringSet(definedMessage))
        {
            if (message == null)
            {
                message = definedMessage;
            }
            else
            {
                message += definedMessage;
            }
        }

        if (message != null)
        {
            caseResult.setMessage(message.trim());
        }

        idToCase.put(id, caseResult);

        expectEndTag(ELEMENT_TEST_RESULT, reader);
        nextTagOrEnd(reader);
    }

    private String getMessage(XMLStreamReader reader) throws XMLStreamException
    {
        StringBuilder message = new StringBuilder();

        nextTagOrEnd(reader);
        while (nextSiblingTag(reader, ELEMENT_ERROR_INFO))
        {
            nextTagOrEnd(reader);

            while (nextSiblingTag(reader, ELEMENT_MESSAGE, ELEMENT_STACK_TRACE))
            {
                appendToMessage(reader, message);
                nextTagOrEnd(reader);
            }

            expectEndTag(ELEMENT_ERROR_INFO, reader);
            nextTagOrEnd(reader);
        }

        expectEndTag(ELEMENT_OUTPUT, reader);
        nextTagOrEnd(reader);
        
        return message.toString().trim();
    }

    private void appendToMessage(XMLStreamReader reader, StringBuilder message) throws XMLStreamException
    {
        String text = getElementText(reader);
        if (text != null)
        {
            if (message.length() > 0)
            {
                message.append("\n");
            }

            message.append(text);
        }
    }

    private long getDuration(Map<String, String> attributes)
    {
        String durationValue  = attributes.get(ATTRIBUTE_DURATION);
        try
        {
            if (durationValue != null)
            {
                String[] pieces = durationValue.split(":");
                if (pieces.length == 3)
                {
                    int hours = Integer.parseInt(pieces[0]);
                    int minutes = Integer.parseInt(pieces[1]);
                    double seconds = Double.parseDouble(pieces[2]);
                    return (long) (hours * Constants.HOUR + minutes * Constants.MINUTE + seconds * Constants.SECOND);
                }
            }
        }
        catch (NumberFormatException e)
        {
            // Fall through.
        }

        return -1;
    }

    private Outcome getOutcome(String name, Map<String, String> attributes)
    {
        String outcomeValue = attributes.get(ATTRIBUTE_OUTCOME);
        if (outcomeValue == null)
        {
            LOG.warning("No outcome for test '" + name + "', assuming error");
            return Outcome.Error;
        }
        
        try
        {
            return Outcome.valueOf(outcomeValue);
        }
        catch (IllegalArgumentException e)
        {
            LOG.warning("Unrecognised test outcome '" + outcomeValue + "', ignoring test '" + name + "'");
            return null;
        }
    }
}
