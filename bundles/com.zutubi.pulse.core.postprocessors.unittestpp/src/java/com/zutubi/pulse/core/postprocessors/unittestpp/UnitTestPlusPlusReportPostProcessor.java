package com.zutubi.pulse.core.postprocessors.unittestpp;

import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.core.postprocessors.XMLTestReportPostProcessorSupport;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.util.Map;
import java.util.TreeMap;

/**
 */
public class UnitTestPlusPlusReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final String ELEMENT_TEST = "test";
    private static final String ELEMENT_FAILURE = "failure";

    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TIME = "time";
    private static final String ATTRIBUTE_SUITE = "suite";

    private Map<String, TestSuiteResult> suites;

    public UnitTestPlusPlusReportPostProcessor()
    {
        super("UnitTest++");
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        Element root = doc.getRootElement();

        suites = new TreeMap<String, TestSuiteResult>();

        // We should get FailedTests and SuccessfulTests sections
        Elements testElements = root.getChildElements(ELEMENT_TEST);
        for(int i = 0; i < testElements.size(); i++)
        {
            processTest(testElements.get(i));
        }

        addSuites(tests);
    }

    private void processTest(Element element)
    {
        String suite = element.getAttributeValue(ATTRIBUTE_SUITE);
        String name = element.getAttributeValue(ATTRIBUTE_NAME);
        long duration = getDuration(element);

        if(suite != null && name != null)
        {
            TestSuiteResult suiteResult = getSuite(suite);
            Element failure = element.getFirstChildElement(ELEMENT_FAILURE);
            TestCaseResult caseResult;
            if(failure == null)
            {
                caseResult = new TestCaseResult(name, duration);
            }
            else
            {
                caseResult = new TestCaseResult(name, duration, TestCaseResult.Status.FAILURE, failure.getAttributeValue(ATTRIBUTE_MESSAGE));
            }
            suiteResult.add(caseResult);
        }
    }

    private long getDuration(Element element)
    {
        String value = element.getAttributeValue(ATTRIBUTE_TIME);
        if(value != null)
        {
            try
            {
                return (long) (Double.parseDouble(value) * 1000);
            }
            catch (NumberFormatException e)
            {
                // Fall through
            }
        }

        return -1;
    }

    private void addSuites(TestSuiteResult tests)
    {
        for(TestSuiteResult suite: suites.values())
        {
            tests.add(suite);
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
}
