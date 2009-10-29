package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.api.XMLStreamUtils;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;

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
        if (isElement(getConfig().getSuiteElement(), reader))
        {
            processSuite(reader, tests);
        }
        else if (isElement(ELEMENT_SUITES, reader))
        {
            processSuites(reader, tests);
        }
    }

    protected void processSuites(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_SUITES, reader);
        reader.nextTag();

        while (reader.isStartElement())
        {
            if (isElement(getConfig().getSuiteElement(), reader))
            {
                processSuite(reader, tests);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndTag(ELEMENT_SUITES, reader);
    }

    private void processSuite(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(getConfig().getSuiteElement(), reader);
        Map<String, String> attributes = XMLStreamUtils.getAttributes(reader);

        String name = getTestSuiteName(attributes);
        if (name.length() == 0)
        {
            nextElement(reader);
            return;
        }

        long duration = getDuration(attributes);
        TestSuiteResult suite = new TestSuiteResult(name, duration);
        tests.addSuite(suite);

        reader.nextTag();

        while (reader.isStartElement())
        {
            if (isElement(getConfig().getSuiteElement(), reader))
            {
                processSuite(reader, suite);
            }
            else if (isElement(getConfig().getCaseElement(), reader))
            {
                processCase(reader, suite);
            }
            else
            {
                nextElement(reader);
            }
        }

        expectEndTag(getConfig().getSuiteElement(), reader);
        reader.nextTag();
    }

    private String getTestSuiteName(Map<String, String> attributes)
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

        Map<String, String> attributes = XMLStreamUtils.getAttributes(reader);
        String name = attributes.get(getConfig().getNameAttribute());
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
        TestCaseResult caseResult = new TestCaseResult(name, duration, TestStatus.PASS);
        suite.addCase(caseResult);
        reader.nextTag();

        String tagName = reader.getLocalName();
        if (tagName.equals(getConfig().getErrorElement()))
        {
            caseResult.setStatus(TestStatus.ERROR);
            caseResult.setMessage(getMessage(reader));
            reader.nextTag();
        }
        else if (tagName.equals(getConfig().getFailureElement()))
        {
            caseResult.setStatus(TestStatus.FAILURE);
            caseResult.setMessage(getMessage(reader));
            reader.nextTag();
        }
        else if (tagName.equals(getConfig().getSkippedElement()))
        {
            caseResult.setStatus(TestStatus.SKIPPED);
            nextElement(reader);
        }

        expectEndTag(getConfig().getCaseElement(), reader);
        reader.nextTag();
    }

    private String getMessage(XMLStreamReader reader) throws XMLStreamException
    {
        Map<String, String> attributes = XMLStreamUtils.getAttributes(reader);
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
