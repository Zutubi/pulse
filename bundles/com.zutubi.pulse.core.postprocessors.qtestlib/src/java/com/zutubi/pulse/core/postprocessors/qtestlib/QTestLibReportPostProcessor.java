package com.zutubi.pulse.core.postprocessors.qtestlib;

import com.zutubi.pulse.core.postprocessors.api.StAXTestReportPostProcessorSupport;
import com.zutubi.pulse.core.postprocessors.api.TestCaseResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.core.postprocessors.api.TestSuiteResult;
import static com.zutubi.pulse.core.util.api.XMLStreamUtils.*;
import com.zutubi.util.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Post-processor for qtestlib XML reports.  See:
 * http://doc.trolltech.com/4.1/qtestlib-tutorial1.html.
 */
public class QTestLibReportPostProcessor extends StAXTestReportPostProcessorSupport
{
    private static final String ELEMENT_TEST_CASE = "TestCase";
    private static final String ELEMENT_TEST_FUNCTION = "TestFunction";
    private static final String ELEMENT_INCIDENT = "Incident";
    private static final String ELEMENT_DATA_TAG = "DataTag";
    private static final String ELEMENT_DESCRIPTION = "Description";
    private static final String ELEMENT_MESSAGE = "Message";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_LINE = "line";

    private static final String TYPE_WARNING = "warn";
    private static final String TYPE_INFO = "info";

    private static final Map<String, TestStatus> STATUS_MAP = new HashMap<String, TestStatus>();
    static
    {
        STATUS_MAP.put("pass", TestStatus.PASS);
        STATUS_MAP.put("fail", TestStatus.FAILURE);
        STATUS_MAP.put("skip", TestStatus.SKIPPED);
    }

    public QTestLibReportPostProcessor(QTestLibReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void process(XMLStreamReader reader, TestSuiteResult tests) throws XMLStreamException
    {
        expectStartTag(ELEMENT_TEST_CASE, reader);
        Map<String, String> attributes = getAttributes(reader);

        String name = attributes.get(ATTRIBUTE_NAME);
        if (name == null)
        {
            return;
        }

        reader.nextTag();

        TestSuiteResult suite = new TestSuiteResult(name);
        tests.addSuite(suite);

        while (nextSiblingTag(reader, ELEMENT_TEST_FUNCTION))
        {
            handleCase(reader, suite);
        }

        expectEndTag(ELEMENT_TEST_CASE, reader);
    }

    private void handleCase(XMLStreamReader reader, TestSuiteResult suite) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        String name = attributes.get(ATTRIBUTE_NAME);
        if (name == null)
        {
            nextElement(reader);
            return;
        }

        reader.nextTag();

        TestCaseResult caseResult = new TestCaseResult(name);
        suite.addCase(caseResult);

        while (nextSiblingTag(reader, ELEMENT_INCIDENT, ELEMENT_MESSAGE))
        {
            String element = reader.getLocalName();
            extractStatusAndMessage(reader, caseResult);
            expectEndTag(element, reader);
            reader.nextTag();
        }

        expectEndTag(ELEMENT_TEST_FUNCTION, reader);
        reader.nextTag();
    }

    private void extractStatusAndMessage(XMLStreamReader reader, TestCaseResult caseResult) throws XMLStreamException
    {
        Map<String, String> attributes = getAttributes(reader);
        caseResult.setStatus(getStatus(attributes));
        getMessage(reader, attributes, caseResult);
    }

    private TestStatus getStatus(Map<String, String> attributes)
    {
        String typeString = attributes.get(ATTRIBUTE_TYPE);
        TestStatus status = STATUS_MAP.get(typeString);
        if (status == null)
        {
            status = TestStatus.PASS;
        }

        return status;
    }

    private void getMessage(XMLStreamReader reader, Map<String, String> attributes, TestCaseResult caseResult) throws XMLStreamException
    {
        String message = getLocation(attributes);

        String typeString = attributes.get(ATTRIBUTE_TYPE);
        if (TYPE_WARNING.equals(typeString))
        {
            message += "Warning: ";
        }
        else if (TYPE_INFO.equals(typeString))
        {
            skipElement(reader);
            return;
        }

        reader.nextTag();
        while (nextSiblingTag(reader, ELEMENT_DESCRIPTION, ELEMENT_DATA_TAG))
        {
            if (reader.getLocalName().equals(ELEMENT_DATA_TAG))
            {
                message += "Data Tag: '" + getElementText(reader) + "': ";
            }
            else
            {
                message += getElementText(reader);
            }
            
            reader.nextTag();
        }

        if (StringUtils.stringSet(message))
        {
            String currentMessage = caseResult.getMessage();
            if (StringUtils.stringSet(currentMessage))
            {
                message = currentMessage + "\n" + message;
            }
            
            caseResult.setMessage(message);
        }
    }

    private String getLocation(Map<String, String> attributes)
    {
        String message = "";
        String file = attributes.get(ATTRIBUTE_FILE);
        if (StringUtils.stringSet(file))
        {
            message += file;
            String line = attributes.get(ATTRIBUTE_LINE);
            if (StringUtils.stringSet(line))
            {
                message += ":" + line;
            }

            message += ": ";
        }

        return message;
    }
}
