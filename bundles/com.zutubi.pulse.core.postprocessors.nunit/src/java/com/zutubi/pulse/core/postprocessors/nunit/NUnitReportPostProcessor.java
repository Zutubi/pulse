package com.zutubi.pulse.core.postprocessors.nunit;

import com.zutubi.pulse.core.postprocessors.api.StAXTestReportPostProcessorSupport;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

/**
 * Post-processor for NUnit version 2 (and compatible) XML reports.  See:
 * http://www.nunit.org/.
 */
public class NUnitReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_ROOT        = "test-results";
    private static final String ELEMENT_SUITE       = "test-suite";
    private static final String ELEMENT_RESULTS     = "results";
    private static final String ELEMENT_CASE        = "test-case";
    private static final String ELEMENT_FAILURE     = "failure";
    private static final String ELEMENT_MESSAGE     = "message";
    private static final String ELEMENT_STACK_TRACE = "stack-trace";
    private static final String ELEMENT_REASON      = "reason";

    private static final String ATTRIBUTE_NAME     = "name";
    private static final String ATTRIBUTE_SUCCESS  = "success";
    private static final String ATTRIBUTE_EXECUTED = "executed";
    private static final String ATTRIBUTE_TIME     = "time";

    public NUnitReportPostProcessor(NUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_ROOT, reader);
        reader.nextTag();
        while (nextSiblingTag(reader, ELEMENT_SUITE))
        {
            // We skip over the top-level suite tag, which contains
            // information about the assembly the tests were in.
            reader.nextTag();
            while (nextSiblingTag(reader, ELEMENT_RESULTS))
            {
                processTopSuiteResults(tests, reader);
            }

            expectEndTag(ELEMENT_SUITE, reader);
            reader.nextTag();
        }

        expectEndTag(ELEMENT_ROOT, reader);
    }

    private void processTopSuiteResults(TestSuiteResult tests, XMLStreamReader reader) throws XMLStreamException
    {
        reader.nextTag();
        while (nextSiblingTag(reader, ELEMENT_SUITE))
        {
            processSuite(tests, "", reader);
        }

        expectEndTag(ELEMENT_RESULTS, reader);
        reader.nextTag();
    }

    private void processSuite(TestSuiteResult parentSuite, String parentPath, XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);

        reader.nextTag();
        while (nextSiblingTag(reader, ELEMENT_RESULTS))
        {
            processSuiteResults(parentSuite, parentPath, reader, attributes);

            expectEndTag(ELEMENT_RESULTS, reader);
            reader.nextTag();
        }

        expectEndTag(ELEMENT_SUITE, reader);
        reader.nextTag();
    }

    private void processSuiteResults(TestSuiteResult parentSuite, String parentPath, XMLStreamReader reader, Map<String, String> attributes) throws XMLStreamException
    {
        String name = attributes.get(ATTRIBUTE_NAME);
        if (name == null)
        {
            nextElement(reader);
            return;
        }

        name = StringUtils.stripPrefix(name, parentPath);

        TestSuiteResult suite = addSuite(parentSuite, name, getDuration(attributes));
        String suitePath = appendPath(parentPath, suite.getName());

        reader.nextTag();
        while (nextSiblingTag(reader, ELEMENT_SUITE, ELEMENT_CASE))
        {
            if (reader.getLocalName().equals(ELEMENT_SUITE))
            {
                processSuite(suite, suitePath, reader);
            }
            else if (reader.getLocalName().equals(ELEMENT_CASE))
            {
                processCase(suite, suitePath, reader);
            }
        }
    }

    private String appendPath(String parentPath, String suiteName)
    {
        if (StringUtils.stringSet(parentPath))
        {
            return parentPath + suiteName + ".";
        }
        else
        {
            return suiteName + ".";
        }
    }

    private TestSuiteResult addSuite(TestSuiteResult parentSuite, String name, long duration)
    {
        TestSuiteResult suite = parentSuite.findSuite(name);
        if (suite == null)
        {
            suite = new TestSuiteResult(name, duration);
            parentSuite.addSuite(suite);
        }

        return suite;
    }

    private void processCase(TestSuiteResult suite, String suitePath, XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        String name = attributes.get(ATTRIBUTE_NAME);
        if (name == null)
        {
            nextElement(reader);
            return;
        }

        name = StringUtils.stripPrefix(name, suitePath);

        TestStatus status = getStatus(attributes);
        TestCaseResult caseResult = new TestCaseResult(name, getDuration(attributes), status);
        if (status == TestStatus.FAILURE)
        {
            caseResult.setMessage(getMessage(reader, ELEMENT_FAILURE));
        }
        else if (status == TestStatus.SKIPPED)
        {
            caseResult.setMessage(getMessage(reader, ELEMENT_REASON));
        }
        else
        {
            skipElement(reader);
        }

        suite.addCase(caseResult);

        expectEndTag(ELEMENT_CASE, reader);
        reader.nextTag();
    }

    private String getMessage(XMLStreamReader reader, String tagName) throws XMLStreamException
    {
        StringBuilder message = new StringBuilder();

        reader.nextTag();
        while (nextSiblingTag(reader, tagName))
        {
            reader.nextTag();

            while (nextSiblingTag(reader, ELEMENT_MESSAGE, ELEMENT_STACK_TRACE))
            {
                appendToMessage(reader, message);
                reader.nextTag();
            }

            expectEndTag(tagName, reader);
            reader.nextTag();
        }

        return message.toString().trim();
    }

    private void appendToMessage(XMLStreamReader reader, StringBuilder message) throws XMLStreamException
    {
        String text = reader.getElementText();
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
        String durationValue  = attributes.get(ATTRIBUTE_TIME);
        try
        {
            if (durationValue != null)
            {
                return (long) (Double.valueOf(durationValue) * Constants.SECOND);
            }
        }
        catch (NumberFormatException e)
        {
            // Fall through.
        }

        return -1;
    }

    private TestStatus getStatus(Map<String, String> attributes)
    {
        boolean executed = getBooleanAttribute(attributes, ATTRIBUTE_EXECUTED, true);
        if (executed)
        {
            boolean success = getBooleanAttribute(attributes, ATTRIBUTE_SUCCESS, false);
            return success ? TestStatus.PASS : TestStatus.FAILURE;
        }
        else
        {
            return TestStatus.SKIPPED;
        }
    }

    private boolean getBooleanAttribute(Map<String, String> attributes, String name, boolean defaultValue)
    {
        String value = attributes.get(name);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }
}
