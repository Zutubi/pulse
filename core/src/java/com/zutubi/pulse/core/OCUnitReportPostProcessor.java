package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * <class-comment/>
 */
public class OCUnitReportPostProcessor implements PostProcessor
{
    private static final Logger LOG = Logger.getLogger(OCUnitReportPostProcessor.class);

    private String name;

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
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return this;
    }

    public void process(File outputDir, StoredFileArtifact artifact, CommandResult result)
    {
        File file = new File(outputDir, artifact.getPath());
        if (!file.isFile())
        {
            return;
        }

        try
        {
            reader = new BufferedReader(new FileReader(file));

            // read until you locate the start of a test suite.
            try
            {
                processFile(artifact);
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

    private void processFile(StoredFileArtifact artifact) throws IOException
    {
        // look for a TestSuite.
        currentLine = nextLine();
        while (currentLine != null)
        {
            // does this line start with Test Suite
            if (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                // we have a test suite.
                artifact.addTest(processSuite());
            }
            currentLine = nextLine();
        }
    }

    private TestSuiteResult processSuite() throws IOException
    {
        // Test Suite 'SenInterfaceTestCase' started at 2006-07-13 23:22:58 +1000

        // process the test suite started line.
        Matcher m = START_SUITE_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
//            // programming error,
            throw new IllegalStateException();
        }

        TestSuiteResult suite = new TestSuiteResult(m.group(1));

        currentLine = nextLine();

        // options.
        // a) a test suite.
        if (START_SUITE_PATTERN.matcher(currentLine).matches())
        {
            while (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                suite.add(processSuite());
            }
        }
        else
        {
            // b) a test case. NOTE: These suites may be empty.
            TestCaseResult child = processCase();
            while (child != null)
            {
                suite.add(child);
                child = processCase();
            }
        }

        // Test Suite 'SenInterfaceTestCase' finished at 2006-07-13 23:22:58 +1000.
        m = END_SUITE_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
            // step forward one line.
            currentLine = nextLine();
            m = END_SUITE_PATTERN.matcher(currentLine);
            if (!m.matches())
            {
                throw new IllegalStateException();
            }
        }

        if (m.group(1).compareTo(suite.getName()) != 0)
        {
            throw new IllegalStateException();
        }

        currentLine = nextLine();

        // Executed 0 tests, with 0 failures (0 unexpected) in 0.000 (0.000) seconds
        m = SUITE_SUMMARY_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
            throw new IllegalStateException();
        }

        suite.setDuration((long) (Double.parseDouble(m.group(4)) * 1000));

        // next line is empty.
        currentLine = nextLine();
        currentLine = nextLine();

        return suite;
    }

    private TestCaseResult processCase() throws IOException
    {
        if (END_SUITE_PATTERN.matcher(currentLine).matches())
        {
            return null;
        }

        String caseOutput = "";
        Matcher m = CASE_SUMMARY_PATTERN.matcher(currentLine);
        while (!m.matches())
        {
            caseOutput = caseOutput + currentLine;
            currentLine = nextLine();
            if (currentLine == null)
            {
                // end of file, end silently..
                return null;
            }
            m = CASE_SUMMARY_PATTERN.matcher(currentLine);
        }

        TestCaseResult result = new TestCaseResult(m.group(2));
        result.setMessage(caseOutput);

        String statusString = m.group(3);
        if (statusString.compareTo("passed") == 0)
        {
            result.setStatus(TestCaseResult.Status.PASS);
        }
        else if (statusString.compareTo("failed") == 0)
        {
            result.setStatus(TestCaseResult.Status.FAILURE);
        }
        result.setDuration((long) (Double.parseDouble(m.group(4)) * 1000));

        currentLine = nextLine();
        return result;
    }

    private String nextLine() throws IOException
    {
        currentLine = reader.readLine();
        return currentLine;
    }
}
