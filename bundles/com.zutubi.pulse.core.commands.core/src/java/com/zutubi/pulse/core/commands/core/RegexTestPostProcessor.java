package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A post-processor that extracts test case results by parsing text lines with
 * regular expressions.
 */
public class RegexTestPostProcessor extends TestReportPostProcessorSupport
{
    private static final Logger LOG = Logger.getLogger(RegexTestPostProcessor.class);

    private LineNumberReader reader;

    public RegexTestPostProcessor(RegexTestPostProcessorConfiguration config)
    {
        super(config);
    }

    @Override
    public RegexTestPostProcessorConfiguration getConfig()
    {
        return (RegexTestPostProcessorConfiguration) super.getConfig();
    }

    public void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        try
        {
            reader = new LineNumberReader(new FileReader(file));
            try
            {
                processFile(tests, ppContext);
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

    private void processFile(TestSuiteResult tests, PostProcessorContext ppContext) throws IOException
    {
        RegexTestPostProcessorConfiguration config = getConfig();
        Map<String, TestStatus> statusMap = config.getStatusMap();

        // clean up any whitespace from the regex which may have been added via the setText()
        String regex = config.getRegex();
        if (config.isTrim())
        {
            regex = regex.trim();
        }

        Pattern pattern = Pattern.compile(regex);

        String currentLine = reader.readLine();
        while (currentLine != null)
        {
            Matcher m = pattern.matcher(currentLine);
            if (m.matches())
            {
                String statusString = m.group(config.getStatusGroup());
                if (config.isAutoFail() || statusMap.containsKey(statusString))
                {
                    String testName = m.group(config.getNameGroup());
                    if (StringUtils.stringSet(testName))
                    {
                        String message = null;
                        if (config.hasDetailsGroup())
                        {
                            message = m.group(config.getDetailsGroup());
                        }

                        String suiteName = null;
                        if (config.hasSuiteGroup())
                        {
                            suiteName = m.group(config.getSuiteGroup());
                        }

                        TestStatus status = statusMap.get(statusString);
                        if (status == null)
                        {
                            // Must be auto-fail case
                            status = TestStatus.FAILURE;
                        }

                        long duration = readDuration(ppContext, config, regex, currentLine, m);

                        TestCaseResult testCaseResult = new TestCaseResult(testName, duration, status, message);

                        // Determine which suite we should add the test case to.
                        TestSuiteResult suite = tests;
                        if (StringUtils.stringSet(suiteName))
                        {
                            suite = tests.findSuite(suiteName);
                            if (suite == null)
                            {
                                suite = new TestSuiteResult(suiteName);
                                tests.addSuite(suite);
                            }
                        }
                        suite.addCase(testCaseResult);
                    }
                    else
                    {
                        ppContext.addFeature(new Feature(Feature.Level.WARNING, currentLine + ": Line matches expression '" + regex + "' but has no test case name (name group: " + config.getNameGroup() + ")", reader.getLineNumber()));
                    }
                }
                else
                {
                    ppContext.addFeature(new Feature(Feature.Level.WARNING, currentLine + ": Test with unrecognised status '" + statusString + "'", reader.getLineNumber()));
                }
            }
            currentLine = reader.readLine();
        }
    }

    private long readDuration(PostProcessorContext ppContext, RegexTestPostProcessorConfiguration config, String regex, String currentLine, Matcher m)
    {
        long duration = TestResult.DURATION_UNKNOWN;
        if (config.hasDurationGroup())
        {
            String durationString = m.group(config.getDurationGroup());
            if (durationString != null)
            {
                try
                {
                    duration = Long.valueOf(durationString);
                }
                catch (NumberFormatException e)
                {
                    ppContext.addFeature(new Feature(Feature.Level.WARNING, currentLine + ": Line matches expression '" + regex + "' but was expecting millisecond duration. Instead found: '" + durationString +"'", reader.getLineNumber()));
                }
            }
        }
        return duration;
    }
}
