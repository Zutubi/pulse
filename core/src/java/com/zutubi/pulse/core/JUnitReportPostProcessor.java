/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.util.IOUtils;
import nu.xom.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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

    public JUnitReportPostProcessor()
    {
        super("JUnit");
    }

    protected void processDocument(Document doc, StoredFileArtifact artifact)
    {
        Element root = doc.getRootElement();
        if(root.getLocalName().equals(ELEMENT_SUITE))
        {
            // A single suite
            processSuite(root, artifact);
        }
        else
        {
            // Looks like a full report, search for suites
            Elements suiteElements = root.getChildElements(ELEMENT_SUITE);
            for(int i = 0; i < suiteElements.size(); i++)
            {
                processSuite(suiteElements.get(i), artifact);
            }
        }
    }

    private void processSuite(Element element, StoredFileArtifact artifact)
    {
        String name = "";

        String attr = element.getAttributeValue(ATTRIBUTE_PACKAGE);
        if(attr != null)
        {
            name += attr + '.';
        }

        attr = element.getAttributeValue(ATTRIBUTE_NAME);
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
        Elements cases = element.getChildElements(ELEMENT_CASE);
        for(int i = 0; i < cases.size(); i++)
        {
            processCase(cases.get(i), suite);
        }
        artifact.addTest(suite);
    }

    private void processCase(Element element, TestSuiteResult suite)
    {
        String name = element.getAttributeValue(ATTRIBUTE_NAME);
        if(name == null)
        {
            // Ignore nameless tests
            return;
        }

        long duration = getDuration(element);
        TestCaseResult caseResult = new TestCaseResult(name, duration);
        suite.add(caseResult);

        Element child = element.getFirstChildElement(ELEMENT_ERROR);
        if(child != null)
        {
            caseResult.setStatus(TestCaseResult.Status.ERROR);

            getMessage(child, caseResult);

            // Prefer error over failure (we *should* not get both, but just
            // in case).
            return;
        }

        child = element.getFirstChildElement(ELEMENT_FAILURE);
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
        String attr = element.getAttributeValue(ATTRIBUTE_TIME);

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
