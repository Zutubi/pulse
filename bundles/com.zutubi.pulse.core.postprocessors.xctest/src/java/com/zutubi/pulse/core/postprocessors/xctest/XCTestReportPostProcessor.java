/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.postprocessors.xctest;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post-processor for XCTest test reports.
 */
public class XCTestReportPostProcessor extends TestReportPostProcessorSupport
{
    private static final Logger LOG = Logger.getLogger(XCTestReportPostProcessor.class);

    private static final Pattern START_SUITE_PATTERN = Pattern.compile("Test Suite '(.*?)' started at (.*$)");
    private static final Pattern END_SUITE_PATTERN = Pattern.compile("Test Suite '(.*?)' (passed|failed) at (.*$)");
    private static final Pattern SUITE_SUMMARY_PATTERN = Pattern.compile("\\s*Executed (\\d*) test[s]?, with (\\d*) failure[s]? \\((\\d*) unexpected\\) in (\\d*\\.\\d*) \\((\\d*\\.\\d*)\\) second[s]?$");
    private static final Pattern START_CASE_PATTERN = Pattern.compile("Test Case '-\\[(.*?) (.*?)\\]' started\\.$");
    private static final Pattern END_CASE_PATTERN = Pattern.compile("Test Case '-\\[(.*?) (.*?)\\]' (.*?) \\((\\d*\\.\\d*) second[s]?\\)\\.$");

    private BufferedReader reader;
    private String currentLine;

    public XCTestReportPostProcessor(XCTestReportPostProcessorConfiguration config)
    {
        super(config);
    }

    protected void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult suite)
    {
        try
        {
            reader = new BufferedReader(new FileReader(file));
            try
            {
                processFile(suite, ppContext);
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
        currentLine = nextLine();
        while (currentLine != null)
        {
            if (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                tests.addSuite(processSuite(ppContext));
            }
            currentLine = nextLine();
        }

        shortenChildSuiteNamesIfRequested(tests);
    }

    private TestSuiteResult processSuite(PostProcessorContext ppContext) throws IOException
    {
        Matcher m = START_SUITE_PATTERN.matcher(currentLine);
        if (!m.matches())
        {
            throw new IllegalStateException("Should only get here by already checking we are at the start of suite marker");
        }

        TestSuiteResult suite = new TestSuiteResult(m.group(1));

        currentLine = nextLine();

        // now we are in the suite, looking for the end suite...
        String caseOutput = "";
        while (currentLine != null && !END_SUITE_PATTERN.matcher(currentLine).matches())
        {
            // if new suite, then recurse.
            if (START_SUITE_PATTERN.matcher(currentLine).matches())
            {
                suite.addSuite(processSuite(ppContext));
            }
            // if test case, then create it.
            else if (END_CASE_PATTERN.matcher(currentLine).matches())
            {
                Matcher caseMatch = END_CASE_PATTERN.matcher(currentLine);
                caseMatch.matches();

                TestCaseResult result = new TestCaseResult(caseMatch.group(2));
                if (StringUtils.stringSet(caseOutput))
                {
                    result.setMessage(caseOutput);
                }

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
            else if (!(START_CASE_PATTERN.matcher(currentLine).matches()))
            {
                // else, add to text.
                if (caseOutput.length() > 0)
                {
                    caseOutput += "\n";
                }
                caseOutput += currentLine;
            }
            currentLine = nextLine();
        }

        if (currentLine == null)
        {
            // Hit EOF looking for end of suite, warn and just process what
            // we have.
            ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, String.format("Reached end of file looking for end of suite '%s' in XCTest report", suite.getName())));
        }
        else
        {
            m = END_SUITE_PATTERN.matcher(currentLine);
            // verify that we are reading the end suite here.
            if (!m.matches())
            {
                throw new IllegalStateException("Should only get here by already checking we are at the end of suite marker");
            }

            if (m.group(1).compareTo(suite.getName()) != 0)
            {
                // Mismatched suites
                ppContext.addFeatureToCommand(new Feature(Feature.Level.WARNING, String.format("Suite name mismatch in XCTest report: expecting '%s' found '%s'", suite.getName(), m.group(1))));
            }

            currentLine = nextLine();
            while (currentLine != null)
            {
                // Executed 0 tests, with 0 failures (0 unexpected) in 0.000 (0.000) seconds
                m = SUITE_SUMMARY_PATTERN.matcher(currentLine);
                if (m.matches())
                {
                    break;
                }

                currentLine = nextLine();
            }

            if (currentLine == null)
            {
                ppContext.addFeature(new Feature(Feature.Level.WARNING, String.format("Reached end of file looking for summary for suite '%s' in XCTest report", suite.getName())));
            }
            else
            {
                suite.setDuration((long) (Double.parseDouble(m.group(4)) * 1000));
            }
        }

        shortenChildSuiteNamesIfRequested(suite);
        return suite;
    }

    private String nextLine() throws IOException
    {
        currentLine = reader.readLine();
        return currentLine;
    }

    private void shortenChildSuiteNamesIfRequested(TestSuiteResult parentSuite)
    {
        if (!((XCTestReportPostProcessorConfiguration) getConfig()).isShortenSuiteNames())
        {
            return;
        }

        // This implementation is naive, but we expect:
        //   a) the number of suites to be small enough.
        //   b) the last elements of suites to usually be unique
        List<TestSuiteResult> suites = parentSuite.getSuites();
        List<List<String>> splitNames = new LinkedList<List<String>>();
        for (TestSuiteResult suite: suites)
        {
            String[] elements = StringUtils.split(suite.getName(), File.separatorChar);
            splitNames.add(Arrays.asList(elements));
        }

        Iterator<TestSuiteResult> it = suites.iterator();
        for (List<String> splitName : splitNames)
        {
            TestSuiteResult suite = it.next();
            int elementCount = splitName.size();
            for (int i = 1; i < elementCount; i++)
            {
                if (countMatchingSuffixes(splitNames, splitName, i) == 1)
                {
                    suite.setName(StringUtils.join(File.separator, splitName.subList(elementCount - i, elementCount)));
                    break;
                }
            }
        }
    }

    private int countMatchingSuffixes(List<List<String>> names, List<String> name, int suffixLength)
    {
        List<String> subname = name.subList(name.size() - suffixLength, name.size());
        int count = 0;
        for (List<String> candidate : names)
        {
            int size = candidate.size();
            if (size >= suffixLength && candidate.subList(size - suffixLength, size).equals(subname))
            {
                count++;
            }
        }

        return count;
    }
}
