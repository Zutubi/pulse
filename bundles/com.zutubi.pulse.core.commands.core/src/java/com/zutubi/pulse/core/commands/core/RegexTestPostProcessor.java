package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.postprocessors.api.*;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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

    private BufferedReader reader;

    private String currentLine;

    private String regex;
    private int statusGroup;
    private int nameGroup;
    private int detailsGroup = -1;

    private boolean autoFail = false;
    private boolean trim = true;
    
    private Map<String, TestStatus> statusMap = new HashMap<String, TestStatus>();

    public RegexTestPostProcessor()
    {
        // provide some defaults.
        this.statusMap.put("PASS", TestStatus.PASS);
        this.statusMap.put("FAILURE", TestStatus.FAILURE);
        this.statusMap.put("ERROR", TestStatus.ERROR);
        this.statusMap.put("SKIPPED", TestStatus.SKIPPED);
    }

    public RegexTestPostProcessor(String name)
    {
        setName(name);
    }

    public void extractTestResults(File file, PostProcessorContext ppContext, TestSuiteResult tests)
    {
        // clean up any whitespace from the regex which may have been added via the setText()
        if (trim)
        {
            regex = regex.trim();
        }

        try
        {
            reader = new BufferedReader(new FileReader(file));

            // read until you locate the start of a test suite.
            try
            {
                processFile(tests);
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
        Pattern pattern = Pattern.compile(regex);

        currentLine = nextLine();
        while (currentLine != null)
        {
            Matcher m = pattern.matcher(currentLine);
            if (m.matches())
            {
                String statusString = m.group(statusGroup);
                if(autoFail || statusMap.containsKey(statusString))
                {
                    String testName = m.group(nameGroup);
                    String message = null;
                    if (detailsGroup >= 0)
                    {
                        message = m.group(detailsGroup);
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

    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    public String getRegex()
    {
        return regex;
    }

    public void setStatusGroup(int i)
    {
        this.statusGroup = i;
    }

    public int getStatusGroup()
    {
        return statusGroup;
    }

    public void setNameGroup(int i)
    {
        this.nameGroup = i;
    }

    public int getNameGroup()
    {
        return nameGroup;
    }

    public int getDetailsGroup()
    {
        return detailsGroup;
    }

    public void setDetailsGroup(int detailsGroup)
    {
        this.detailsGroup = detailsGroup;
    }

    public String getPassStatus()
    {
        return findStatus(TestStatus.PASS);
    }

    public void setPassStatus(String status)
    {
        this.statusMap.put(status, TestStatus.PASS);
    }

    public String getFailureStatus()
    {
        return findStatus(TestStatus.FAILURE);
    }

    public void setFailureStatus(String status)
    {
        this.statusMap.put(status, TestStatus.FAILURE);
    }

    public String getErrorStatus()
    {
        return findStatus(TestStatus.ERROR);
    }

    public void setErrorStatus(String status)
    {
        this.statusMap.put(status, TestStatus.ERROR);
    }

    public String getSkippedStatus()
    {
        return findStatus(TestStatus.SKIPPED);
    }

    public void setSkippedStatus(String status)
    {
        this.statusMap.put(status, TestStatus.SKIPPED);
    }

    private String findStatus(TestStatus status)
    {
        for(Map.Entry<String, TestStatus> entry: statusMap.entrySet())
        {
            if(entry.getValue().equals(status))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public void setText(String txt)
    {
        // first lets check to see if there is a valid regex here.
        if (TextUtils.stringSet(txt) && TextUtils.stringSet(txt.trim()))
        {
            regex = txt;
        }
    }

    public void setAutoFail(boolean autoFail)
    {
        this.autoFail = autoFail;
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }
}
