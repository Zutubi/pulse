package com.zutubi.pulse.core.postprocessors.ocunit;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <class-comment/>
 */
public class OCUnitReportPostProcessor extends TestReportPostProcessorSupport
{
    private static final Logger LOG = Logger.getLogger(OCUnitReportPostProcessor.class);

    private BufferedReader reader;

    private String currentLine;
    private static final Pattern START_SUITE_PATTERN = Pattern.compile("Test Suite '(.*?)' started at (.*$)");
    private static final Pattern END_SUITE_PATTERN = Pattern.compile("Test Suite '(.*?)' finished at (.*$)");
    private static final Pattern SUITE_SUMMARY_PATTERN = Pattern.compile("Executed (\\d*) test[s]?, with (\\d*) failure[s]? \\((\\d*) unexpected\\) in (\\d*\\.\\d*) \\((\\d*\\.\\d*)\\) second[s]?$");
    private static final Pattern CASE_SUMMARY_PATTERN = Pattern.compile("Test Case '-\\[(.*?) (.*?)\\]' (.*?) \\((\\d*\\.\\d*) second[s]?\\)\\.$");

    public OCUnitReportPostProcessor()
    {

    }

    public OCUnitReportPostProcessor(String name)
    {
        setName(name);
    }
    
    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult suite)
    {
        try
        {
            reader = new BufferedReader(new FileReader(file));

            // read until you locate the start of a test suite.
            try
            {
                processFile(suite);
            }
            catch (IllegalStateException e)
            {
                // we have come across something we do not understand. Leave the test content
                // as it is and move on.
                LOG.info(e);
            }

        }
        catch (IOException e)
        {
            LOG.info(e);
        }
        finally
        {
            IOUtils.close(reader);
        }
    }

    private void processFile(TestSuiteResult tests) throws IOException
    {
        // look for a TestSuite.
        currentLine = nextLine();
        while (currentLine != null)
        {
            // does this line start with Test Suite
            if (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                // we have a test suite.
                tests.addSuite(processSuite());
            }
            currentLine = nextLine();
        }
    }

    private TestSuiteResult processSuite() throws IOException
    {
        // varify that we have a start suite here.
        Matcher m = START_SUITE_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
            throw new IllegalStateException();
        }

        // start the suite.
        TestSuiteResult suite = new TestSuiteResult(m.group(1));

        currentLine = nextLine();

        // now we are in the suite, looking for the end suite...
        String caseOutput = "";
        while (!END_SUITE_PATTERN.matcher(currentLine).matches())
        {
            // if new suite, then recurse.
            if (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                suite.addSuite(processSuite());
            }
            // if test case, then create it.
            else if (CASE_SUMMARY_PATTERN.matcher(currentLine).matches())
            {
                Matcher caseMatch = CASE_SUMMARY_PATTERN.matcher(currentLine);
                caseMatch.matches();

                TestCaseResult result = new TestCaseResult(caseMatch.group(2));
                result.setMessage(caseOutput);

                String statusString = caseMatch.group(3);
                if (statusString.compareTo("passed") == 0)
                {
                    result.setStatus(TestStatus.PASS);
                }
                else if (statusString.compareTo("failed") == 0)
                {
                    result.setStatus(TestStatus.FAILURE);
                }
                result.setDuration((long) (Double.parseDouble(caseMatch.group(4)) * 1000));
                suite.addCase(result);
                caseOutput = "";
            }
            else
            {
                // else, add to text.
                caseOutput = caseOutput + currentLine;
            }
            currentLine = nextLine();
        }

        // verify that we are reading the end suite here.
        m = END_SUITE_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
            throw new IllegalStateException();
        }

        if (m.group(1).compareTo(suite.getName()) != 0)
        {
            throw new IllegalStateException();
        }

        currentLine = nextLine();

        // Executed 0 tests, with 0 failures (0 unexpected) in 0.000 (0.000) seconds
        m = SUITE_SUMMARY_PATTERN.matcher(currentLine);
        while (!m.matches())
        {
            currentLine = nextLine();
            m = SUITE_SUMMARY_PATTERN.matcher(currentLine);
        }

        suite.setDuration((long) (Double.parseDouble(m.group(4)) * 1000));

        return suite;
    }

    private String nextLine() throws IOException
    {
        currentLine = reader.readLine();
        return currentLine;
    }
}
