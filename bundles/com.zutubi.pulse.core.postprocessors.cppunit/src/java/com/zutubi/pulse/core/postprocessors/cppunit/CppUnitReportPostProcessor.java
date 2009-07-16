package com.zutubi.pulse.core.postprocessors.cppunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.api.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.Map;
import java.util.TreeMap;

/**
 */
public class CppUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final String ELEMENT_SUCCESSFUL_TESTS = "SuccessfulTests";
    private static final String ELEMENT_TEST = "Test";
    private static final String ELEMENT_FAILED_TESTS = "FailedTests";
    private static final String ELEMENT_FAILED_TEST = "FailedTest";
    private static final String ELEMENT_NAME = "Name";
    private static final String ELEMENT_FAILURE_TYPE = "FailureType";
    private static final String ELEMENT_LOCATION = "Location";
    private static final String ELEMENT_FILE = "File";
    private static final String ELEMENT_LINE = "Line";
    private static final String ELEMENT_MESSAGE = "Message";

    private static final String FAILURE_TYPE_ERROR = "Error";

    private Map<String, TestSuiteResult> suites;

    public CppUnitReportPostProcessor(CppUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        Element root = doc.getRootElement();

        // CIB-755: the post processor must be stateless, as it can be used
        // to process multiple reports.  Recreate this map each time.
        suites = new TreeMap<String, TestSuiteResult>();

        // We should get FailedTests and SuccessfulTests sections
        Elements testElements = root.getChildElements(ELEMENT_FAILED_TESTS);
        for(int i = 0; i < testElements.size(); i++)
        {
            processFailedTests(testElements.get(i));
        }

        testElements = root.getChildElements(ELEMENT_SUCCESSFUL_TESTS);
        for(int i = 0; i < testElements.size(); i++)
        {
            processSuccessfulTests(testElements.get(i));
        }

        addSuites(tests);
    }

    private void processFailedTests(Element element)
    {
        Elements elements = element.getChildElements(ELEMENT_FAILED_TEST);
        for(int i = 0; i < elements.size(); i++)
        {
            // We expect name, failure type, location and message child elements
            Element testElement = elements.get(i);
            String[] name = getTestName(testElement);

            TestStatus status = getStatus(testElement);
            String message = getMessage(testElement);

            TestSuiteResult suite = getSuite(name[0]);
            TestCaseResult result = new TestCaseResult(name[1], TestResult.DURATION_UNKNOWN, status, message);
            suite.addCase(result);
        }
    }

    private void processSuccessfulTests(Element element)
    {
        // We expect a bunch of Test's with Name subelements
        Elements elements = element.getChildElements(ELEMENT_TEST);
        for(int i = 0; i < elements.size(); i++)
        {
            Element testElement = elements.get(i);
            String[] name = getTestName(testElement);

            TestSuiteResult suite = getSuite(name[0]);
            TestCaseResult result = new TestCaseResult(name[1]);
            suite.addCase(result);
        }
    }

    private void addSuites(TestSuiteResult tests)
    {
        for(TestSuiteResult suite: suites.values())
        {
            tests.addSuite(suite);
        }
    }

    private TestSuiteResult getSuite(String name)
    {
        if(suites.containsKey(name))
        {
            return suites.get(name);
        }
        else
        {
            TestSuiteResult suite = new TestSuiteResult(name);
            suites.put(name, suite);
            return suite;
        }
    }

    private String[] getTestName(Element testElement)
    {
        Element nameElement = testElement.getFirstChildElement(ELEMENT_NAME);
        if(nameElement == null)
        {
            return null;
        }

        String name = XMLUtils.getText(nameElement);
        if(name == null)
        {
            return null;
        }

        String[] bits = name.split("::", 2);
        if(bits.length == 1)
        {
            return new String[]{ "[unknown]", bits[0] };
        }
        else
        {
            return bits;
        }
    }

    private TestStatus getStatus(Element element)
    {
        TestStatus status = TestStatus.FAILURE;

        Element typeElement = element.getFirstChildElement(ELEMENT_FAILURE_TYPE);
        if(typeElement != null)
        {
            String type = XMLUtils.getText(typeElement);
            if(type != null && type.equals(FAILURE_TYPE_ERROR))
            {
                status = TestStatus.ERROR;
            }
        }

        return status;
    }

    private String getMessage(Element element)
    {
        String message = "";

        // Include the location if available.
        Element locationElement = element.getFirstChildElement(ELEMENT_LOCATION);
        if(locationElement != null)
        {
            String location = "At";
            Element fileElement = locationElement.getFirstChildElement(ELEMENT_FILE);
            if(fileElement != null)
            {
                String file = XMLUtils.getText(fileElement);
                if(file != null)
                {
                    location += " file " + file;
                }
            }

            Element lineElement = locationElement.getFirstChildElement(ELEMENT_LINE);
            if(lineElement != null)
            {
                String line = XMLUtils.getText(lineElement);
                if(line != null)
                {
                    location += " line " + line;
                }
            }

            message += location + "\n";
        }

        Element messageElement = element.getFirstChildElement(ELEMENT_MESSAGE);
        if(messageElement != null)
        {
            String text = XMLUtils.getText(messageElement);
            if(text != null)
            {
                message += text;
            }
        }

        return message;
    }
}
