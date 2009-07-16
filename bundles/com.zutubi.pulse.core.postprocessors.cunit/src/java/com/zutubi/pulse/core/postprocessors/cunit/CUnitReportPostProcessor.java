package com.zutubi.pulse.core.postprocessors.cunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.logging.Logger;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.XMLException;
import org.xml.sax.SAXException;

/**
 * Post-processor for CUnit version 2 (and compatible) XML reports.  See:
 * http://cunit.sourceforge.net/.
 */
public class CUnitReportPostProcessor extends XMLTestReportPostProcessorSupport
{
    private static final Logger LOG = Logger.getLogger(CUnitReportPostProcessor.class);

    private static final String ELEMENT_RESULT_LISTING       = "CUNIT_RESULT_LISTING";
    private static final String ELEMENT_RUN_SUITE            = "CUNIT_RUN_SUITE";
    private static final String ELEMENT_RUN_SUITE_SUCCESS    = "CUNIT_RUN_SUITE_SUCCESS";
    private static final String ELEMENT_RUN_SUITE_FAILURE    = "CUNIT_RUN_SUITE_FAILURE";
    private static final String ELEMENT_SUITE_NAME           = "SUITE_NAME";
    private static final String ELEMENT_SUITE_FAILURE_REASON = "FAILURE_REASON";
    private static final String ELEMENT_RUN_TEST             = "CUNIT_RUN_TEST_RECORD";
    private static final String ELEMENT_RUN_TEST_SUCCESS     = "CUNIT_RUN_TEST_SUCCESS";
    private static final String ELEMENT_RUN_TEST_FAILURE     = "CUNIT_RUN_TEST_FAILURE";
    private static final String ELEMENT_TEST_NAME            = "TEST_NAME";
    private static final String ELEMENT_TEST_FILE_NAME       = "FILE_NAME";
    private static final String ELEMENT_TEST_LINE_NUMBER     = "LINE_NUMBER";
    private static final String ELEMENT_TEST_CONDITION       = "CONDITION";

    public CUnitReportPostProcessor(CUnitReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected Builder createBuilder()
    {
        Builder builder = new Builder();
        try
        {
            builder.setLoadExternalDTD(false);
        }
        catch (SAXException e)
        {
            LOG.severe("Unable to instruct parser to ignore external DTD; it is likely post-processing will fail.", e);
        }

        return builder;
    }

    protected void processDocument(Document doc, final TestSuiteResult tests)
    {
        Element root = doc.getRootElement();
        XMLUtils.forEachChild(root, ELEMENT_RESULT_LISTING, new UnaryProcedure<Element>()
        {
            public void process(Element element)
            {
                XMLUtils.forEachChild(element, ELEMENT_RUN_SUITE, new UnaryProcedure<Element>()
                {
                    public void process(Element element)
                    {
                        XMLUtils.forEachChild(element, ELEMENT_RUN_SUITE_SUCCESS, new UnaryProcedure<Element>()
                        {
                            public void process(Element element)
                            {
                                handleSuite(tests, element);
                            }
                        });

                        XMLUtils.forEachChild(element, ELEMENT_RUN_SUITE_FAILURE, new UnaryProcedure<Element>()
                        {
                            public void process(Element element)
                            {
                                handleFailedSuite(tests, element);
                            }
                        });
                    }
                });
            }
        });
    }

    private void handleSuite(TestSuiteResult tests, Element element)
    {
        try
        {
            String suiteName = XMLUtils.getRequiredChildText(element, ELEMENT_SUITE_NAME, true);
            final TestSuiteResult suite = new TestSuiteResult(suiteName);
            XMLUtils.forEachChild(element, ELEMENT_RUN_TEST, new UnaryProcedure<Element>()
            {
                public void process(Element element)
                {
                    processCase(suite, element);
                }
            });

            tests.addSuite(suite);
        }
        catch (XMLException e)
        {
            LOG.warning("Ignoring malformed suite: " + e.getMessage(), e);
        }
    }

    private void processCase(TestSuiteResult suite, Element element)
    {
        try
        {
            Element success = element.getFirstChildElement(ELEMENT_RUN_TEST_SUCCESS);
            if(success != null)
            {
                suite.addCase(new TestCaseResult(XMLUtils.getRequiredChildText(success, ELEMENT_TEST_NAME, true)));
            }
            else
            {
                Element failure = element.getFirstChildElement(ELEMENT_RUN_TEST_FAILURE);
                if(failure != null)
                {
                    String name = XMLUtils.getRequiredChildText(failure, ELEMENT_TEST_NAME, true);
                    String message = getFailureMessage(failure);

                    TestCaseResult caseResult = suite.findCase(name);
                    if(caseResult == null)
                    {
                        suite.addCase(new TestCaseResult(name, TestResult.DURATION_UNKNOWN, TestStatus.FAILURE, message));
                    }
                    else
                    {
                        // Another failed assertion already - tag this one on.
                        caseResult.setMessage(caseResult.getMessage() + '\n' + message);
                    }
                }
            }
        }
        catch (XMLException e)
        {
            LOG.warning("Ignoring malformed case: " + e.getMessage(), e);
        }
    }

    private String getFailureMessage(Element element)
    {
        String file = XMLUtils.getChildText(element, ELEMENT_TEST_FILE_NAME, "<unknown file>").trim();
        String line = XMLUtils.getChildText(element, ELEMENT_TEST_LINE_NUMBER, "-1").trim();
        String condition = XMLUtils.getChildText(element, ELEMENT_TEST_CONDITION, "").trim();
        return String.format("%s: %s: %s", file, line, condition);
    }

    private void handleFailedSuite(TestSuiteResult tests, Element element)
    {
        // The suite didn't run properly at all, but record a special case to
        // show what happened.
        try
        {
            String name = XMLUtils.getRequiredChildText(element, ELEMENT_SUITE_NAME, true);
            String failureReason = XMLUtils.getRequiredChildText(element, ELEMENT_SUITE_FAILURE_REASON, true);
            TestSuiteResult suite = new TestSuiteResult(name);
            suite.addCase(new TestCaseResult("Suite Failure Notification", TestResult.DURATION_UNKNOWN, TestStatus.ERROR, failureReason));
            tests.addSuite(suite);
        }
        catch(XMLException e)
        {
            LOG.warning("Ignoring malformed suite failure: " + e.getMessage(), e);
        }
    }
}
