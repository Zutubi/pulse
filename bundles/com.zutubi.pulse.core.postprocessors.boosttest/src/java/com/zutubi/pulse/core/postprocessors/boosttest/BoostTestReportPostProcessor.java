package com.zutubi.pulse.core.postprocessors.boosttest;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.XMLUtils;
import com.zutubi.util.StringUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.LinkedList;
import java.util.List;

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

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_FILE = "file";
    private static final String ATTRIBUTE_LINE = "line";

    public BoostTestReportPostProcessor(BoostTestReportPostProcessorConfiguration config)
    {
        super(config);
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
            parentSuite.addSuite(suiteResult);
        }
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
            TestStatus status;
            String message = null;

            String exception = getException(element);
            if (exception == null)
            {
                List<String> errors = getErrors(element);
                if (errors.isEmpty())
                {
                    status = TestStatus.PASS;
                }
                else
                {
                    status = TestStatus.FAILURE;
                    message = StringUtils.join("\n", errors);
                }
            }
            else
            {
                status = TestStatus.ERROR;
                message = "uncaught exception: " + exception;
            }

            parentSuite.addCase(new TestCaseResult(name, duration, status, message));
        }

    }

    private String getException(Element element)
    {
        Element exceptionElement = element.getFirstChildElement(ELEMENT_EXCEPTION);
        if (exceptionElement != null)
        {
            return XMLUtils.getText(exceptionElement);
        }
        
        return null;
    }

    private List<String> getErrors(Element element)
    {
        List<String> errors = new LinkedList<String>();
        Elements childElements = element.getChildElements();
        for (int i = 0; i < childElements.size(); i++)
        {
            Element child = childElements.get(i);
            String name = child.getLocalName();
            if (name.equals(ELEMENT_ERROR) || name.equals(ELEMENT_FATAL_ERROR))
            {
                errors.add(getError(child));
            }
        }

        return errors;
    }

    private String getError(Element element)
    {
        String error = element.getLocalName().replace('_', ' ').toLowerCase();
        String file = element.getAttributeValue(ATTRIBUTE_FILE);
        if (file != null)
        {
            error += ": " + file;

            String line = element.getAttributeValue(ATTRIBUTE_LINE);
            if (line != null)
            {
                error += ":" + line;
            }
        }
        
        error += ": " + XMLUtils.getText(element, "");
        return error;
    }

    private long getDuration(Element element)
    {
        String durationText = XMLUtils.getChildText(element, ELEMENT_TESTING_TIME,
                                                    Long.toString(TestResult.DURATION_UNKNOWN));
        try
        {
            return (long) (Double.parseDouble(durationText) * 1000);
        }
        catch (NumberFormatException e)
        {
            // Fall through
        }

        return TestResult.DURATION_UNKNOWN;
    }
}
