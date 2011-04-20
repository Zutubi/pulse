package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import static com.zutubi.pulse.core.scm.hg.MercurialConstants.*;
import com.zutubi.pulse.core.scm.process.api.ScmLineHandlerSupport;
import com.zutubi.pulse.core.scm.process.api.ScmOutputCapturingHandler;
import com.zutubi.pulse.core.scm.process.api.ScmOutputHandler;
import com.zutubi.pulse.core.scm.process.api.ScmProcessRunner;
import com.zutubi.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * A wrapper around the hg command line.
 */
public class MercurialCore
{
    private ScmProcessRunner runner;

    /**
     * Creates a hg command line wrapper with no inactivity timeout.
     */
    public MercurialCore()
    {
        this(0);
    }

    /**
     * Creates a git command line wrapper with the given inactivity timeout.
     *
     * @param inactivityTimeout number of seconds of inactivity (no output)
     *                          after which to timeout a git subprocess
     */
    public MercurialCore(int inactivityTimeout)
    {
        runner = new ScmProcessRunner("hg");
        runner.setInactivityTimeout(inactivityTimeout);
    }

    /**
     * Set the working directory in which the hg commands will be run.
     *
     * @param dir working directory to run hg withing
     */
    public void setWorkingDirectory(File dir)
    {
        if (dir == null)
        {
            throw new IllegalArgumentException("Working directory is required");
        }

        if (!dir.isDirectory())
        {
            throw new IllegalArgumentException("Working directory '" + dir.getAbsolutePath() + "' does not exist");
        }

        runner.setDirectory(dir);
    }

    public InputStream cat(String path, String revision) throws ScmException
    {
        ScmOutputCapturingHandler handler = new ScmOutputCapturingHandler(Charset.defaultCharset());
        run(handler, COMMAND_CAT, FLAG_REVISION, revision, path);
        return new ByteArrayInputStream(handler.getOutput().getBytes());
    }

    public void clone(ScmOutputHandler handler, String repository, String branch, String revision, String dir) throws ScmException
    {
        List<String> command = new LinkedList<String>();
        command.add(COMMAND_CLONE);
        command.add(FLAG_NO_UPDATE);

        if (StringUtils.stringSet(branch))
        {
            command.add(FLAG_BRANCH);
            command.add(branch);
        }

        if (StringUtils.stringSet(revision))
        {
            command.add(FLAG_REVISION);
            command.add(revision);
        }

        command.add(repository);
        command.add(dir);

        run(handler, command.toArray(new String[command.size()]));
    }

    public void update(ScmOutputHandler handler, String revision) throws ScmException
    {
        if (revision == null)
        {
            run(handler, COMMAND_UPDATE);
        }
        else
        {
            run(handler, COMMAND_UPDATE, FLAG_REVISION, revision);
        }
    }

    public void pull(ScmOutputHandler handler, String branch) throws ScmException
    {
        if (StringUtils.stringSet(branch))
        {
            run(handler, COMMAND_PULL, FLAG_BRANCH, branch);
        }
        else
        {
            run(handler, COMMAND_PULL);
        }
    }

    public List<Changelist> log(boolean verbose, String branch, String beginRevision, String endRevision, int limit) throws ScmException
    {
        List<String> command = new LinkedList<String>();
        if (verbose)
        {
            command.add(FLAG_VERBOSE);
        }
        
        command.add(COMMAND_LOG);
        command.add(FLAG_STYLE);
        command.add(STYLE_XML);

        if (limit != -1)
        {
            command.add(FLAG_LIMIT);
            command.add(Integer.toString(limit));
        }

        if (StringUtils.stringSet(branch))
        {
            command.add(FLAG_BRANCH);
            command.add(branch);
        }

        command.add(FLAG_REVISION);
        command.add(getRevisionArg(beginRevision, endRevision));

        ScmOutputCapturingHandler handler = new ScmOutputCapturingHandler(Charset.defaultCharset());

        run(handler, command.toArray(new String[command.size()]));

        return LogParser.parse(handler.getOutput());
    }

    private String getRevisionArg(String beginRevision, String endRevision)
    {
        String revisionArg = "";
        if (beginRevision != null)
        {
            revisionArg += beginRevision;
        }

        revisionArg += ":";

        if (endRevision != null)
        {
            revisionArg += endRevision;
        }
        return revisionArg;
    }

    public void tag(ScmOutputHandler handler, Revision revision, String name, String message, boolean force) throws ScmException
    {
        List<String> commands = new LinkedList<String>();
        commands.add(COMMAND_TAG);
        if (force)
        {
            commands.add(FLAG_FORCE);
        }
        commands.add(FLAG_MESSAGE);
        commands.add(message);
        commands.add(FLAG_REVISION);
        commands.add(revision.getRevisionString());
        commands.add(name);

        run(handler, commands.toArray(new String[commands.size()]));
    }

    public Map<String, String> tags() throws ScmException
    {
        final Map<String, String> result = new HashMap<String, String>();
        run(new ScmLineHandlerSupport()
        {
            @Override
            public void handleStdout(String line)
            {
                String[] pieces = line.split("\\s+");
                if (pieces.length == 2)
                {
                    int colonIndex = pieces[1].indexOf(':');
                    if (colonIndex > 0 && colonIndex < pieces[1].length() - 1)
                    {
                        result.put(pieces[0], pieces[1].substring(colonIndex + 1));
                    }
                }
            }
        }, COMMAND_TAGS);

        return result;
    }


    public String parents() throws ScmException
    {
        ScmOutputCapturingHandler handler = new ScmOutputCapturingHandler(Charset.defaultCharset());
        run(handler, COMMAND_PARENTS, FLAG_TEMPLATE, TEMPLATE_NODE);
        return handler.getOutput();
    }

    public void push(ScmOutputHandler handler, String branch) throws ScmException
    {
        if (StringUtils.stringSet(branch))
        {
            run(handler, COMMAND_PUSH, FLAG_BRANCH, branch);
        }
        else
        {
            run(handler, COMMAND_PUSH);
        }
    }

    protected int run(ScmOutputHandler handler, String... commands) throws ScmException
    {
        return run(handler, null, true, commands);
    }

    protected int run(ScmOutputHandler handler, String input, boolean checkExitCode, String... commands) throws ScmException
    {
        String[] actualCommands = new String[commands.length + 1];
        actualCommands[0] = getMercurialCommand();
        System.arraycopy(commands, 0, actualCommands, 1, commands.length);
        return runner.runProcess(handler, input == null ? null : input.getBytes(), checkExitCode, actualCommands);
    }

    protected String getMercurialCommand()
    {
        return System.getProperty(PROPERTY_HG_COMMAND, DEFAULT_HG);
    }

    /**
     * Provide command line style access to running hg commands for testing.
     *
     * @param argv command line arguments
     * @throws IOException if an error occurs.
     */
    public static void main(String... argv) throws IOException
    {
        if (argv.length == 0)
        {
            System.out.println("Please enter the full hg command you with to execute.");
            return;
        }

        ScmLineHandlerSupport outputHandler = new ScmLineHandlerSupport()
        {
            public void handleStdout(String line)
            {
                System.out.println(line);
            }

            public void handleStderr(String line)
            {
                System.err.println(line);
            }
        };

        try
        {
            MercurialCore hg = new MercurialCore();
            hg.setWorkingDirectory(new File("."));
            System.out.println(new File(".").getCanonicalPath());
            if (!Boolean.getBoolean("skip.env"))
            {
                // use a tree map to provide ordering to the keys.
                System.out.println("========= Execution Environment ============");
                Map<String, String> env = new TreeMap<String, String>(hg.runner.getEnvironment());
                for (String key : env.keySet())
                {
                    String value = env.get(key);
                    System.out.println(key + "=" + value);
                }
                System.out.println();
                System.out.println("========= Command output ============");
                System.out.println(StringUtils.join(" ", argv));
            }
            hg.run(outputHandler, null, true, argv);
        }
        catch (ScmException e)
        {
            System.out.println("Exit Status: " + outputHandler.getExitCode());
            e.printStackTrace();
        }
    }
}
