package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A post-processor that extracts test case results by parsing text lines with
 * regular expressions.
 */
public class RegexTestPostProcessor<T extends RegexTestPostProcessorConfiguration> extends TestReportPostProcessorSupport<T>
{
    private static final Logger LOG = Logger.getLogger(RegexTestPostProcessor.class);

    private BufferedReader reader;

    private String currentLine;

    public RegexTestPostProcessor(T config)
    {
        super(config);
    }


    public void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        // clean up any whitespace from the regex which may have been added via the setText()
        T config = getConfig();
        String regex = config.getRegex();
        if (config.isTrim())
        {
            regex = regex.trim();
        }

        try
        {
            reader = new BufferedReader(new FileReader(file));

            // read until you locate the start of a test suite.
            try
            {
                processFile(tests, regex);
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

    private void processFile(TestSuiteResult tests, String regex) throws IOException
    {
        T config = getConfig();
        Map<String, TestStatus> statusMap = config.getStatusMap();

        Pattern pattern = Pattern.compile(regex);

        currentLine = nextLine();
        while (currentLine != null)
        {
            Matcher m = pattern.matcher(currentLine);
            if (m.matches())
            {
                String statusString = m.group(config.getStatusGroup());
                if(config.isAutoFail() || statusMap.containsKey(statusString))
                {
                    String testName = m.group(config.getNameGroup());
                    String message = null;
                    if (config.getDetailsGroup() >= 0)
                    {
                        message = m.group(config.getDetailsGroup());
                    }

                    TestStatus status = statusMap.get(statusString);
                    if(status == null)
                    {
                        // Must be auto-fail case
                        status = TestStatus.FAILURE;
                    }

                    tests.addCase(new TestCaseResult(testName, TestResult.DURATION_UNKNOWN, status, message));
                }
                else
                {
                    LOG.warning("Test with unrecognised status '" + statusString + "'");
                }
            }
            currentLine = nextLine();
        }
    }

    private String nextLine() throws IOException
    {
        currentLine = reader.readLine();
        return currentLine;
    }
}
