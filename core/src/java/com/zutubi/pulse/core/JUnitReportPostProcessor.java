package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import nu.xom.*;

/**
 */
public class JUnitReportPostProcessor extends XMLReportPostProcessor
{
    private static final String ELEMENT_SUITE = "testsuite";
    private static final String ELEMENT_CASE = "testcase";
    private static final String ELEMENT_ERROR = "error";
    private static final String ELEMENT_FAILURE = "failure";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PACKAGE = "package";
    private static final String ATTRIBUTE_TIME = "time";

    private String suiteElement = ELEMENT_SUITE;
    private String caseElement = ELEMENT_CASE;
    private String errorElement = ELEMENT_ERROR;
    private String failureElement = ELEMENT_FAILURE;
    private String nameAttribute = ATTRIBUTE_NAME;
    private String packageAttribute = ATTRIBUTE_PACKAGE;
    private String timeAttribute = ATTRIBUTE_TIME;

    public JUnitReportPostProcessor()
    {
        super("JUnit");
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        Element root = doc.getRootElement();
        if(root.getLocalName().equals(suiteElement))
        {
            // A single suite
            processSuite(root, tests);
        }
        else
        {
            // Looks like a full report, search for suites
            Elements suiteElements = root.getChildElements(suiteElement);
            for(int i = 0; i < suiteElements.size(); i++)
            {
                processSuite(suiteElements.get(i), tests);
            }
        }
    }

    private void processSuite(Element element, TestSuiteResult tests)
    {
        String name = "";

        String attr = element.getAttributeValue(packageAttribute);
        if(attr != null)
        {
            name += attr + '.';
        }

        attr = element.getAttributeValue(nameAttribute);
        if(attr != null)
        {
            name += attr;
        }

        if(name.length() == 0)
        {
            // No name?? No dice...
            return;
        }

        long duration = getDuration(element);

        TestSuiteResult suite = new TestSuiteResult(name, duration);
        Elements cases = element.getChildElements(caseElement);
        for(int i = 0; i < cases.size(); i++)
        {
            processCase(cases.get(i), suite);
        }

        tests.add(suite);
    }

    private void processCase(Element element, TestSuiteResult suite)
    {
        String name = element.getAttributeValue(nameAttribute);
        if(name == null)
        {
            // Ignore nameless tests
            return;
        }

        long duration = getDuration(element);
        TestCaseResult caseResult = new TestCaseResult(name, duration);
        suite.add(caseResult);

        Element child = element.getFirstChildElement(errorElement);
        if(child != null)
        {
            caseResult.setStatus(TestCaseResult.Status.ERROR);

            getMessage(child, caseResult);

            // Prefer error over failure (we *should* not get both, but just
            // in case).
            return;
        }

        child = element.getFirstChildElement(failureElement);
        if(child != null)
        {
            caseResult.setStatus(TestCaseResult.Status.FAILURE);
            getMessage(child, caseResult);
        }
    }

    private void getMessage(Element child, TestCaseResult caseResult)
    {
        Node node = child.getChild(0);
        if(node != null && node instanceof Text)
        {
            caseResult.setMessage(node.getValue().trim());
        }
    }

    private long getDuration(Element element)
    {
        long duration = TestResult.UNKNOWN_DURATION;
        String attr = element.getAttributeValue(timeAttribute);

        if(attr != null)
        {
            try
            {
                double time = Double.parseDouble(attr);
                duration = (long)(time * 1000);
            }
            catch(NumberFormatException e)
            {
                // No matter, leave time out
            }
        }

        return duration;
    }

    public void setSuiteElement(String suiteElement)
    {
        this.suiteElement = suiteElement;
    }

    public void setCaseElement(String caseElement)
    {
        this.caseElement = caseElement;
    }

    public void setErrorElement(String errorElement)
    {
        this.errorElement = errorElement;
    }

    public void setFailureElement(String failureElement)
    {
        this.failureElement = failureElement;
    }

    public void setNameAttribute(String nameAttribute)
    {
        this.nameAttribute = nameAttribute;
    }

    public void setPackageAttribute(String packageAttribute)
    {
        this.packageAttribute = packageAttribute;
    }

    public void setTimeAttribute(String timeAttribute)
    {
        this.timeAttribute = timeAttribute;
    }
}
