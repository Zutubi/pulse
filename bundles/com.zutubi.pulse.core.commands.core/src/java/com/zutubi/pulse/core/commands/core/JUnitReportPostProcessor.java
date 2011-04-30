package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

/**
 * Post-processor for junit (and compatible) XML reports.
 */
public class JUnitReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_SUITES = "testsuites";

    public JUnitReportPostProcessor(JUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    @Override
    public JUnitReportPostProcessorConfiguration getConfig()
    {
        return (JUnitReportPostProcessorConfiguration) super.getConfig();
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        while (nextSiblingTag(reader, getConfig().getSuiteElement(), ELEMENT_SUITES))
        {
            if (isElement(getConfig().getSuiteElement(), reader))
            {
                processSuite(reader, tests);
            }
            else if (isElement(ELEMENT_SUITES, reader))
            {
                processSuites(reader, tests);
            }
        }
    }

    protected void processSuites(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_SUITES, reader);
        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, getConfig().getSuiteElement()))
        {
            processSuite(reader, tests);
        }

        expectEndTag(ELEMENT_SUITES, reader);
    }

    private void processSuite(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(getConfig().getSuiteElement(), reader);
        Map<String, String> attributes = getAttributes(reader);

        String name = getTestSuiteName(attributes);
        if (name.length() == 0)
        {
            nextElement(reader);
            return;
        }

        long duration = getDuration(attributes);
        TestSuiteResult suite = new TestSuiteResult(name, duration);
        tests.addSuite(suite);

        nextTagOrEnd(reader);

        while (nextSiblingTag(reader, getConfig().getSuiteElement(), getConfig().getCaseElement()))
        {
            if (isElement(getConfig().getSuiteElement(), reader))
            {
                processSuite(reader, suite);
            }
            else if (isElement(getConfig().getCaseElement(), reader))
            {
                processCase(reader, suite);
            }
        }

        expectEndTag(getConfig().getSuiteElement(), reader);
        nextTagOrEnd(reader);
    }

    protected String getTestSuiteName(Map<String, String> attributes)
    {
        String name = "";
        String attr = attributes.get(getConfig().getPackageAttribute());
        if (attr != null)
        {
            name += attr + '.';
        }

        attr = attributes.get(getConfig().getNameAttribute());
        if (attr != null)
        {
            name += attr;
        }
        return name;
    }

    private void processCase(XMLStreamReader reader, TestSuiteResult suite) throws XMLStreamException
    {
        expectStartTag(getConfig().getCaseElement(), reader);

        Map<String, String> attributes = getAttributes(reader);
        String name = getTestCaseName(attributes);
        if (name == null)
        {
            nextElement(reader);
            return;
        }

        String className = attributes.get(getConfig().getClassAttribute());
        if (className != null && !suite.getName().equals(className))
        {
            name = className + "." + name;
        }

        long duration = getDuration(attributes);
        TestCaseResult caseResult = new TestCaseResult(name, duration, getTestCaseImmediateStatus(attributes));
        suite.addCase(caseResult);
        nextTagOrEnd(reader);

        if (nextSiblingTag(reader, getConfig().getErrorElement(), getConfig().getFailureElement(), getConfig().getSkippedElement()))
        {
            String tagName = reader.getLocalName();
            if (tagName.equals(getConfig().getErrorElement()))
            {
                caseResult.setStatus(TestStatus.ERROR);
                caseResult.setMessage(getMessage(reader));
                nextTagOrEnd(reader);
            }
            else if (tagName.equals(getConfig().getFailureElement()))
            {
                caseResult.setStatus(TestStatus.FAILURE);
                caseResult.setMessage(getMessage(reader));
                nextTagOrEnd(reader);
            }
            else if (tagName.equals(getConfig().getSkippedElement()))
            {
                caseResult.setStatus(TestStatus.SKIPPED);
                nextElement(reader);
            }
        }

        // skip to the end.
        while (reader.isStartElement())
        {
            nextElement(reader);
        }

        expectEndTag(getConfig().getCaseElement(), reader);
        nextTagOrEnd(reader);
    }

    protected String getTestCaseName(Map<String, String> attributes)
    {
        return attributes.get(getConfig().getNameAttribute());
    }

    protected TestStatus getTestCaseImmediateStatus(Map<String, String> attributes)
    {
        return TestStatus.PASS;
    }

    private String getMessage(XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        String message = attributes.get(getConfig().getMessageAttribute());

        String elementText = reader.getElementText();
        if (elementText != null && elementText.length() > 0)
        {
            message = elementText.trim();
        }

        return (message != null && message.length() == 0) ? null : message;
    }

    private long getDuration(Map<String, String> attributes)
    {
        long duration = TestResult.DURATION_UNKNOWN;
        String attr = attributes.get(getConfig().getTimeAttribute());
        if (attr != null)
        {
            try
            {
                double time = Double.parseDouble(attr);
                duration = (long) (time * 1000);
            }
            catch (NumberFormatException e)
            {
                // No matter, leave time out
            }
        }
        return duration;
    }
}
