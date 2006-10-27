package com.zutubi.pulse.scm.p4;

import com.zutubi.pulse.core.model.NumericalRevision;
import com.zutubi.pulse.scm.SCMException;
import static com.zutubi.pulse.scm.p4.P4Constants.*;
import com.zutubi.pulse.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class P4Client
{
    private static final String ASCII_CHARSET = "US-ASCII";

    private ProcessBuilder p4Builder;
    private Pattern changesPattern;
    private Pattern lineSplitterPattern;

    public class P4Result
    {
        public StringBuffer stdout;
        public StringBuffer stderr;
        public int exitCode;

        public P4Result()
        {
            stdout = new StringBuffer();
            stderr = new StringBuffer();
        }
    }

    public P4Client()
    {
        p4Builder = new ProcessBuilder();
        // Output of p4 changes -s submitted -m 1:
        //   Change <number> on <date> by <user>@<client>
        changesPattern = Pattern.compile("^Change ([0-9]+) on (.+) by (.+)@(.+) '(.+)'$", Pattern.MULTILINE);
        lineSplitterPattern = Pattern.compile("\r?\n");
    }

    public void setEnv(String variable, String value)
    {
        if (value != null)
        {
            p4Builder.environment().put(variable, value);
        }
    }

    public void setWorkingDir(File dir)
    {
        p4Builder.directory(dir);
    }

    public P4Result runP4(String input, String... commands) throws SCMException
    {
        return runP4(true, input, commands);
    }

    public P4Result runP4(boolean throwOnStderr, String input, String... commands) throws SCMException
    {
        final P4Result result = new P4Result();

        runP4WithHandler(new P4ErrorDetectingHandler(throwOnStderr)
        {
            public void handleStdout(String line)
            {
                result.stdout.append(line);
                result.stdout.append('\n');
            }

            public void handleExitCode(int code) throws SCMException
            {
                super.handleExitCode(code);
                result.stderr = getStderr();
                result.exitCode = code;
            }
        }, input, commands);

        return result;
    }

    public void runP4WithHandler(P4Handler handler, String input, String... commands) throws SCMException
    {
        Process child;

        p4Builder.command(commands);

        try
        {
            child = p4Builder.start();
        }
        catch (IOException e)
        {
            throw new SCMException("Could not start p4 process", e);
        }

        if (input != null)
        {
            try
            {
                OutputStream stdinStream = child.getOutputStream();

                stdinStream.write(input.getBytes(ASCII_CHARSET));
                stdinStream.close();
            }
            catch (IOException e)
            {
                throw new SCMException("Error writing to input of p4 process", e);
            }
        }

        BufferedReader stdoutReader = null;
        BufferedReader stderrReader = null;

        try
        {
            stdoutReader = new BufferedReader(new InputStreamReader(child.getInputStream(), ASCII_CHARSET));
            stderrReader = new BufferedReader(new InputStreamReader(child.getErrorStream(), ASCII_CHARSET));

            String line;
            while((line = stdoutReader.readLine()) != null)
            {
                handler.handleStdout(line);
            }

            while((line = stderrReader.readLine()) != null)
            {
                handler.handleStderr(line);
            }

            handler.handleExitCode(child.waitFor());
        }
        catch (IOException e)
        {
            throw new SCMException("Error reading output of p4 process", e);
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
        finally
        {
            IOUtils.close(stdoutReader);
            IOUtils.close(stderrReader);
        }
    }

    public void createClient(String templateClient, String clientName, File toDirectory) throws SCMException
    {
        P4Client.P4Result result = runP4(null, P4_COMMAND, FLAG_CLIENT, templateClient, COMMAND_CLIENT, FLAG_OUTPUT);
        String clientSpec = result.stdout.toString();

        clientSpec = clientSpec.replaceAll("\nRoot:.*", Matcher.quoteReplacement("\nRoot: " + toDirectory.getAbsolutePath()));
        clientSpec = clientSpec.replaceAll("\nHost:.*", Matcher.quoteReplacement("\nHost: "));
        clientSpec = clientSpec.replaceAll("\nClient:.*" + templateClient, Matcher.quoteReplacement("\nClient: " + clientName));
        clientSpec = clientSpec.replaceAll("//" + templateClient + "/", Matcher.quoteReplacement("//" + clientName + "/"));
        runP4(clientSpec, P4_COMMAND, COMMAND_CLIENT, FLAG_INPUT);
    }

    public Map<String, String> getServerInfo(String client) throws SCMException
    {
        Map<String, String> info = new TreeMap<String, String>();
        P4Client.P4Result result;

        if (client == null)
        {
            result = runP4(null, P4_COMMAND, COMMAND_INFO);
        }
        else
        {
            result = runP4(null, P4_COMMAND, FLAG_CLIENT, client, COMMAND_INFO);
        }

        for (String line : splitLines(result))
        {
            int index = line.indexOf(':');
            if (index > 0 && index < line.length() - 1)
            {
                info.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
            }
        }

        return info;
    }

    public NumericalRevision getLatestRevisionForFiles(String clientName, String... files) throws SCMException
    {
        List<String> args = new ArrayList<String>(8 + files.length);

        args.add(P4_COMMAND);

        if (clientName != null)
        {
            args.add(FLAG_CLIENT);
            args.add(clientName);
        }

        args.add(COMMAND_CHANGES);
        args.add(FLAG_STATUS);
        args.add(VALUE_SUBMITTED);
        args.add(FLAG_MAXIMUM);
        args.add("1");

        for (String file : files)
        {
            args.add(file);
        }

        P4Client.P4Result result = runP4(null, args.toArray(new String[args.size()]));
        Matcher matcher = changesPattern.matcher(result.stdout);

        if (matcher.find())
        {
            return new NumericalRevision(Long.parseLong(matcher.group(1)));
        }
        else
        {
            return new NumericalRevision(0);
        }
    }

    public long createChangelist(String description) throws SCMException
    {
        P4Client.P4Result result = runP4(null, P4_COMMAND, COMMAND_CHANGE, FLAG_OUTPUT);
        String changeSpec = result.stdout.toString();

        changeSpec = changeSpec.replaceAll("<enter description here>", Matcher.quoteReplacement(description));
        result = runP4(changeSpec, P4_COMMAND, COMMAND_CHANGE, FLAG_INPUT);
        Pattern created = Pattern.compile("Change ([0-9]+) created.");
        String response = result.stdout.toString().trim();
        Matcher m = created.matcher(response);
        if(m.matches())
        {
            return Long.parseLong(m.group(1));
        }
        else
        {
            throw new SCMException("Unrecognised response from p4 change '" + response + "'");
        }
    }

    public void submit(String comment) throws SCMException
    {
        P4Result result = runP4(null, P4_COMMAND, COMMAND_CHANGE, FLAG_OUTPUT);
        String out = result.stdout.toString();
        out = out.replace("<enter description here>", comment);
        runP4(out, P4_COMMAND, COMMAND_SUBMIT, FLAG_INPUT);
    }

    public String[] splitLines(P4Result result)
    {
        return lineSplitterPattern.split(result.stdout);
    }

    public Pattern getChangesPattern()
    {
        return changesPattern;
    }
}
