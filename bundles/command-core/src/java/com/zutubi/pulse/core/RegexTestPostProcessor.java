package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.TestCaseResult;
import com.zutubi.pulse.core.model.TestSuiteResult;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import com.zutubi.util.TextUtils;

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
    enum Resolution
    {
        APPEND,
        OFF,
        PREPEND
    }

    private static final Logger LOG = Logger.getLogger(RegexTestPostProcessor.class);

    private BufferedReader reader;

    private String currentLine;

    private String regex;
    private int statusGroup;
    private int nameGroup;

    private boolean autoFail = false;
    private boolean trim = true;
    private Resolution resolveConflicts = Resolution.OFF;
    
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

                    TestCaseResult result = new TestCaseResult();
                    result.setName(testName);

                    TestCaseResult.Status status = statusMap.get(statusString);
                    if(status == null)
                    {
                        // Must be auto-fail case
                        status = TestCaseResult.Status.FAILURE;
                    }

                    result.setStatus(status);

                    if(resolveConflicts != Resolution.OFF && tests.hasCase(result.getName()))
                    {
                        int addition = 2;
                        while(tests.hasCase(makeCaseName(result.getName(), addition, resolveConflicts)))
                        {
                            addition++;
                        }

                        result.setName(makeCaseName(result.getName(), addition, resolveConflicts));
                    }

                    tests.add(result);
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

    private String makeCaseName(String name, int addition, Resolution resolveConflicts)
    {
        if(resolveConflicts == Resolution.APPEND)
        {
            return name + addition;
        }
        else
        {
            return Integer.toString(addition) + name;
        }
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

    public String getPassStatus()
    {
        for(Map.Entry<String, TestCaseResult.Status> entry: statusMap.entrySet())
        {
            if(entry.getValue().equals(TestCaseResult.Status.PASS))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public void setPassStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.PASS);
    }

    public String getFailureStatus()
    {
        for(Map.Entry<String, TestCaseResult.Status> entry: statusMap.entrySet())
        {
            if(entry.getValue().equals(TestCaseResult.Status.FAILURE))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public void setFailureStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.FAILURE);
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

    public Resolution getResolveConflicts()
    {
        return resolveConflicts;
    }

    public void setResolveConflicts(String resolution) throws FileLoadException
    {
        try
        {
            resolveConflicts = Resolution.valueOf(resolution.toUpperCase());
        }
        catch(IllegalArgumentException e)
        {
            throw new FileLoadException("Unrecognised conflict resolution '" + resolution + "'");
        }
    }
}
