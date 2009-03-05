package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import nu.xom.*;

/**
 */
public class JUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    public JUnitReportPostProcessor(JUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    @Override
    public JUnitReportPostProcessorConfiguration getConfig()
    {
        return (JUnitReportPostProcessorConfiguration) super.getConfig();
    }

    protected void processDocument(Document doc, TestSuiteResult tests)
    {
        String suiteElement = getConfig().getSuiteElement();
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
        JUnitReportPostProcessorConfiguration config = getConfig();
        String name = "";

        String attr = element.getAttributeValue(config.getPackageAttribute());
        if(attr != null)
        {
            name += attr + '.';
        }

        attr = element.getAttributeValue(config.getNameAttribute());
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
        Elements nested = element.getChildElements(config.getSuiteElement());
        for(int i = 0; i < nested.size(); i++)
        {
            processSuite(nested.get(i), suite);
        }

        Elements cases = element.getChildElements(config.getCaseElement());
        for(int i = 0; i < cases.size(); i++)
        {
            processCase(cases.get(i), suite);
        }

        tests.addSuite(suite);
    }

    private void processCase(Element element, TestSuiteResult suite)
    {
        JUnitReportPostProcessorConfiguration config = getConfig();
        String name = element.getAttributeValue(config.getNameAttribute());
        if(name == null)
        {
            // Ignore nameless tests
            return;
        }

        String className = element.getAttributeValue(config.getClassAttribute());
        if(className != null && !suite.getName().equals(className))
        {
            name = className + "." + name;
        }

        long duration = getDuration(element);
        TestCaseResult caseResult = new TestCaseResult(name, duration, TestStatus.PASS);
        suite.addCase(caseResult);

        Element child = element.getFirstChildElement(config.getErrorElement());
        if(child != null)
        {
            caseResult.setStatus(TestStatus.ERROR);

            getMessage(child, caseResult);

            // Prefer error over failure (we *should* not get both, but just
            // in case).
            return;
        }

        child = element.getFirstChildElement(config.getFailureElement());
        if(child != null)
        {
            caseResult.setStatus(TestStatus.FAILURE);
            getMessage(child, caseResult);
            return;
        }

        child = element.getFirstChildElement(config.getSkippedElement());
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
            String message = element.getAttributeValue(getConfig().getMessageAttribute());
            if(message != null)
            {
                caseResult.setMessage(message);
            }
        }
    }

    private long getDuration(Element element)
    {
        long duration = TestResult.DURATION_UNKNOWN;
        String attr = element.getAttributeValue(getConfig().getTimeAttribute());

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
}
