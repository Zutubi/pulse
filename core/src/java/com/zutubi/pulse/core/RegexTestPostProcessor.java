package com.zutubi.pulse.core;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <class comment/>
 */
public class RegexTestPostProcessor extends TestReportPostProcessor
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

    private Map<String, TestCaseResult.Status> statusMap = new HashMap<String, TestCaseResult.Status>();

    public RegexTestPostProcessor()
    {
        // provide some defaults.
        this.statusMap.put("PASS", TestCaseResult.Status.PASS);
        this.statusMap.put("FAILURE", TestCaseResult.Status.FAILURE);
        this.statusMap.put("ERROR", TestCaseResult.Status.ERROR);
    }

    public RegexTestPostProcessor(String name)
    {
        setName(name);
    }

    public void internalProcess(CommandResult result, File file, TestSuiteResult suite)
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

                    TestCaseResult result = new TestCaseResult(testName);
                    if (detailsGroup >= 0)
                    {
                        result.setMessage(m.group(detailsGroup));
                    }

                    TestCaseResult.Status status = statusMap.get(statusString);
                    if(status == null)
                    {
                        // Must be auto-fail case
                        status = TestCaseResult.Status.FAILURE;
                    }

                    result.setStatus(status);
                    tests.add(result, getResolveConflicts());
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
        return findStatus(TestCaseResult.Status.PASS);
    }

    public void setPassStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.PASS);
    }

    public String getFailureStatus()
    {
        return findStatus(TestCaseResult.Status.FAILURE);
    }

    public void setFailureStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.FAILURE);
    }

    public String getErrorStatus()
    {
        return findStatus(TestCaseResult.Status.ERROR);
    }

    public void setErrorStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.ERROR);
    }


    private String findStatus(TestCaseResult.Status status)
    {
        for(Map.Entry<String, TestCaseResult.Status> entry: statusMap.entrySet())
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
