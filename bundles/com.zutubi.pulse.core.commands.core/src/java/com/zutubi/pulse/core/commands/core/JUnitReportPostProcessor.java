package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import nu.xom.*;

/**
 */
public class JUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final String ELEMENT_SUITE   = "testsuite";
    private static final String ELEMENT_CASE    = "testcase";
    private static final String ELEMENT_ERROR   = "error";
    private static final String ELEMENT_FAILURE = "failure";
    private static final String ELEMENT_SKIPPED = "skipped";

    private static final String ATTRIBUTE_CLASS   = "classname";
    private static final String ATTRIBUTE_MESSAGE = "message";
    private static final String ATTRIBUTE_NAME    = "name";
    private static final String ATTRIBUTE_PACKAGE = "package";
    private static final String ATTRIBUTE_TIME    = "time";

    private String suiteElement     = ELEMENT_SUITE;
    private String caseElement      = ELEMENT_CASE;
    private String errorElement     = ELEMENT_ERROR;
    private String failureElement   = ELEMENT_FAILURE;
    private String skippedElement   = ELEMENT_SKIPPED;
    private String classAttribute   = ATTRIBUTE_CLASS;
    private String messageAttribute = ATTRIBUTE_MESSAGE;
    private String nameAttribute    = ATTRIBUTE_NAME;
    private String packageAttribute = ATTRIBUTE_PACKAGE;
    private String timeAttribute    = ATTRIBUTE_TIME;

    public JUnitReportPostProcessor()
    {
        super("JUnit");
    }

    public JUnitReportPostProcessor(String reportType)
    {
        super(reportType);
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
        Elements nested = element.getChildElements(suiteElement);
        for(int i = 0; i < nested.size(); i++)
        {
            processSuite(nested.get(i), suite);
        }

        Elements cases = element.getChildElements(caseElement);
        for(int i = 0; i < cases.size(); i++)
        {
            processCase(cases.get(i), suite);
        }

        tests.addSuite(suite);
    }

    private void processCase(Element element, TestSuiteResult suite)
    {
        String name = element.getAttributeValue(nameAttribute);
        if(name == null)
        {
            // Ignore nameless tests
            return;
        }

        String className = element.getAttributeValue(classAttribute);
        if(className != null && !suite.getName().equals(className))
        {
            name = className + "." + name;
        }

        long duration = getDuration(element);
        TestCaseResult caseResult = new TestCaseResult(name, duration, TestStatus.PASS);
        suite.addCase(caseResult);

        Element child = element.getFirstChildElement(errorElement);
        if(child != null)
        {
            caseResult.setStatus(TestStatus.ERROR);

            getMessage(child, caseResult);

            // Prefer error over failure (we *should* not get both, but just
            // in case).
            return;
        }

        child = element.getFirstChildElement(failureElement);
        if(child != null)
        {
            caseResult.setStatus(TestStatus.FAILURE);
            getMessage(child, caseResult);
            return;
        }

        child = element.getFirstChildElement(skippedElement);
        if (child != null)
        {
            caseResult.setStatus(TestStatus.SKIPPED);
        }
    }

    private void getMessage(Element element, TestCaseResult caseResult)
    {
        if (element.getChildCount() > 0)
        {
            Node node = element.getChild(0);
            if(node != null && node instanceof Text)
            {
                caseResult.setMessage(node.getValue().trim());
            }
        }
        else
        {
            String message = element.getAttributeValue(messageAttribute);
            if(message != null)
            {
                caseResult.setMessage(message);
            }
        }
    }

    private long getDuration(Element element)
    {
        long duration = TestResult.DURATION_UNKNOWN;
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

    public void setSkippedElement(String skippedElement)
    {
        this.skippedElement = skippedElement;
    }

    public void setClassAttribute(String classAttribute)
    {
        this.classAttribute = classAttribute;
    }

    public void setMessageAttribute(String messageAttribute)
    {
        this.messageAttribute = messageAttribute;
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
