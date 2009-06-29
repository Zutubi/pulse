package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

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

    private boolean processMessages = false;
    private boolean processInfo = false;

    public BoostTestReportPostProcessor()
    {
        super("Boost.Test");
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        processSuites(doc.getRootElement(), tests);
    }

    private void processSuites(Element containingElement, TestSuiteResult parentSuite)
    {
        Elements suiteElements = containingElement.getChildElements(ELEMENT_TEST_SUITE);
        for (int i = 0; i < suiteElements.size(); i++)
        {
            processSuite(suiteElements.get(i), parentSuite);
        }
    }

    private void processSuite(Element element, TestSuiteResult parentSuite)
    {
        String name = element.getAttributeValue(ATTRIBUTE_NAME);
        if (name != null)
        {
            TestSuiteResult suiteResult = new TestSuiteResult(name);
            processSuites(element, suiteResult);
            processCases(element, suiteResult);
            suiteResult.setDuration(getTotalDuration(suiteResult));
            parentSuite.addSuite(suiteResult);
        }
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

    private void processCases(Element containingElement, TestSuiteResult parentSuite)
    {
        Elements caseElements = containingElement.getChildElements(ELEMENT_TEST_CASE);
        for (int i = 0; i < caseElements.size(); i++)
        {
            processCase(caseElements.get(i), parentSuite);
        }
    }

    private void processCase(Element element, TestSuiteResult parentSuite)
    {
        String name = element.getAttributeValue(ATTRIBUTE_NAME);
        if (name != null)
        {
            long duration = getDuration(element);
            StringBuilder builder = new StringBuilder();
            TestStatus status = processMessages(element, builder);
            parentSuite.addCase(new TestCaseResult(name, duration, status, builder.length() > 0 ? builder.toString() : null));
        }

    }

    private TestStatus processMessages(Element element, StringBuilder builder)
    {
        TestStatus status = TestStatus.PASS;
        Elements children = element.getChildElements();
        for (int i = 0; i < children.size(); i++)
        {
            Element child = children.get(i);
            String name = child.getLocalName();

            TestStatus brokenStatus = ELEMENT_TO_STATUS.get(name);
            if (brokenStatus == null)
            {
                if (processMessages && name.equals(ELEMENT_MESSAGE) || processInfo && name.equals(ELEMENT_INFO))
                {
                    appendMessage(child, builder);
                }
            }
            else
            {
                if (brokenStatus.compareTo(status) > 0)
                {
                    status = brokenStatus;
                }

                appendMessage(child, builder);
            }
        }

        return status;
    }

    private void appendMessage(Element element, StringBuilder builder)
    {
        String message = ELEMENT_TO_MESSAGE.get(element.getLocalName());
        String file = element.getAttributeValue(ATTRIBUTE_FILE);
        if (file != null)
        {
            message += ": " + file;

            String line = element.getAttributeValue(ATTRIBUTE_LINE);
            if (line != null)
            {
                message += ":" + line;
            }
        }
        
        message += ": " + XMLUtils.getText(element, "");
        if (builder.length() > 0)
        {
            builder.append("\n");
        }
        builder.append(message);
    }

    private long getDuration(Element element)
    {
        String durationText = XMLUtils.getChildText(element, ELEMENT_TESTING_TIME,
                                                    Long.toString(TestResult.DURATION_UNKNOWN));
        try
        {
            return (long) (Double.parseDouble(durationText) / 1000);
        }
        catch (NumberFormatException e)
        {
            // Fall through
        }

        return TestResult.DURATION_UNKNOWN;
    }

    public void setProcessMessages(boolean processMessages)
    {
        this.processMessages = processMessages;
    }

    public void setProcessInfo(boolean processInfo)
    {
        this.processInfo = processInfo;
    }
}
