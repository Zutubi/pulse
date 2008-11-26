package com.zutubi.pulse.core.postprocessors.boostregression;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Processes the XML reports generated by Boost's process_jam_log utility.
 */
public class BoostRegressionPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final String ELEMENT_TEST_LOG = "test-log";

    // Actions
    private static final String ELEMENT_COMPILE = "compile";
    private static final String ELEMENT_LIB = "lib";
    private static final String ELEMENT_LINK = "link";
    private static final String ELEMENT_RUN = "run";

    private static final String[] ACTION_ELEMENTS = { ELEMENT_COMPILE, ELEMENT_LIB, ELEMENT_LINK, ELEMENT_RUN };
    
    // Test log attributes
    private static final String ATTRIBUTE_LIBRARY = "library";
    private static final String ATTRIBUTE_TEST_NAME = "test-name";

    // Action attributes
    private static final String ATTRIBUTE_RESULT = "result";

    // Possible results
    private static final String RESULT_FAILURE = "fail";

    public BoostRegressionPostProcessor()
    {
        super("Boost.Regression");
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        Element root = doc.getRootElement();
        if(root.getLocalName().equals(ELEMENT_TEST_LOG))
        {
            // Found a test
            processTestLog(root, tests);
        }
    }

    private void processTestLog(Element element, TestSuiteResult tests)
    {
        String suite = element.getAttributeValue(ATTRIBUTE_LIBRARY);
        String name = element.getAttributeValue(ATTRIBUTE_TEST_NAME);

        if(suite != null && name != null)
        {
            TestSuiteResult suiteResult = getSuite(suite, tests);
            TestStatus status = getStatus(element);
            String details = null;
            if(status != TestStatus.PASS)
            {
                details = getDetails(element);
            }

            suiteResult.addCase(new TestCaseResult(name, TestResult.DURATION_UNKNOWN, status, details));
        }
    }

    private TestStatus getStatus(Element testLogElement)
    {
        Elements children = testLogElement.getChildElements();
        for(int i = 0; i < children.size(); i++)
        {
            Element child = children.get(i);
            if(isAction(child))
            {
                String result = child.getAttributeValue(ATTRIBUTE_RESULT);
                if(RESULT_FAILURE.equals(result))
                {
                    return TestStatus.FAILURE;
                }
            }
        }

        return TestStatus.PASS;
    }

    private String getDetails(Element testLogElement)
    {
        StringBuilder details = new StringBuilder();

        Elements children = testLogElement.getChildElements();
        for(int i = 0; i < children.size(); i++)
        {
            Element child = children.get(i);
            if(isAction(child))
            {
                String content = XMLUtils.getText(child, "", false);
                if(content.trim().length() > 0)
                {
                    details.append("============================[ ");
                    details.append(child.getLocalName());
                    details.append(" output below ]============================");
                    details.append(content);
                    details.append("============================[ ");
                    details.append(child.getLocalName());
                    details.append(" output above ]============================\n");
                }
            }
        }

        return details.toString();
    }

    private boolean isAction(Element element)
    {
        String name = element.getLocalName();
        for(String action: ACTION_ELEMENTS)
        {
            if(action.equals(name))
            {
                return true;
            }
        }

        return false;
    }

    private TestSuiteResult getSuite(String suitePath, TestSuiteResult parentSuite)
    {
        String[] pieces = suitePath.split("/");
        return getSuite(pieces, 0, parentSuite);
    }

    private TestSuiteResult getSuite(String[] path, int index, TestSuiteResult parentSuite)
    {
        if(index == path.length)
        {
            return parentSuite;
        }

        TestSuiteResult suite = parentSuite.findSuite(path[index]);
        if(suite == null)
        {
            suite = new TestSuiteResult(path[index]);
            parentSuite.addSuite(suite);
        }

        return getSuite(path, index + 1, suite);
    }
}
