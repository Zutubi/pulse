package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;
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

    public void internalProcess(StoredFileArtifact artifact, CommandResult result, CommandContext context)
    {
        File file = new File(context.getOutputDir(), artifact.getPath());
        if (!file.isFile())
        {
            return;
        }

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
                processFile(context.getTestResults());
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
                String testName = m.group(nameGroup);
                
                TestCaseResult result = new TestCaseResult();
                result.setName(testName);
                result.setStatus(statusMap.get(statusString));

                tests.add(result);
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

    public void setPassStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.PASS);
    }

    public void setFailureStatus(String status)
    {
        this.statusMap.put(status, TestCaseResult.Status.FAILURE);
    }

    public void setText(String txt)
    {
        regex = txt;
    }

    public void setTrim(boolean trim)
    {
        this.trim = trim;
    }
}
