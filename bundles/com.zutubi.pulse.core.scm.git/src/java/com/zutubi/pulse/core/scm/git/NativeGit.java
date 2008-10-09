package com.zutubi.pulse.core.scm.git;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import com.zutubi.pulse.core.util.process.AsyncProcess;
import com.zutubi.pulse.core.util.process.LineHandler;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * The native git object is a wrapper around the implementation details for running native git operations.
 */
public class NativeGit
{
    private static final Logger LOG = Logger.getLogger(NativeGit.class);
    private static final long PROCESS_TIMEOUT = Long.getLong("pulse.git.inactivity.timeout", 300);
    private static final String ASCII_CHARSET = "US-ASCII";
    /**
     * The date format used to read the 'date' field on git log output.
     */
    private final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    private ScmFeedbackHandler scmHandler;

    private ProcessBuilder git;

    private static final String GIT = "git";
    private static final String COMMAND_PULL = "pull";
    private static final String COMMAND_LOG = "log";
    private static final String COMMAND_CLONE = "clone";
    private static final String COMMAND_CHECKOUT = "checkout";
    private static final String COMMAND_BRANCH = "branch";
    private static final String COMMAND_SHOW = "show";
    private static final String CHECKOUT_OPTION_BRANCH = "-b";
    private static final String LOG_OPTION_NAME_STATUS = "--name-status";
    private static final String LOG_OPTION_PRETTY = "--pretty";
    private static final String LOG_OPTION_CHANGES = "-n";
    private static final String CLONE_OPTION_NO_CHECKOUT = "--no-checkout";
    private static final String LOG_OPTION_REVERSE = "--reverse";
    private static final String BRANCH_OPTION_DELETE = "-D";

    public NativeGit()
    {
        git = new ProcessBuilder();
    }

    /**
     * Set the working directory in which the native git commands will be run.
     *
     * @param dir working directory.
     */
    public void setWorkingDirectory(File dir)
    {
        if (dir == null || !dir.isDirectory())
        {
            throw new IllegalArgumentException("The working directory must be an existing directory.");
        }
        git.directory(dir);
    }

    public void setScmEventHandler(ScmFeedbackHandler scmHandler)
    {
        this.scmHandler = scmHandler;
    }

    public void clone(String repository, String dir) throws ScmException
    {
        run(GIT, COMMAND_CLONE, CLONE_OPTION_NO_CHECKOUT, repository, dir);
    }

    public void pull() throws ScmException
    {
        run(GIT, COMMAND_PULL);
    }

    public InputStream show(String file) throws ScmException
    {
        return show("HEAD", file);
    }

    public InputStream show(String revision, String file) throws ScmException
    {
        String[] commands = {GIT, COMMAND_SHOW, revision + ":" + file};

        final StringBuffer buffer = new StringBuffer();
        OutputHandlerAdapter handler = new OutputHandlerAdapter()
        {
            public void handleStdout(String line)
            {
                buffer.append(line);
            }
        };

        runWithHandler(handler, null, commands);

        if (handler.getExitCode() != 0)
        {
            throw new ScmException("Git command: " + StringUtils.join(" ", commands) + " exited " +
                    "with non zero exit code: " + handler.getExitCode() + ". " + handler.getError());
        }

        return new ByteArrayInputStream(buffer.toString().getBytes());
    }

    public List<GitLogEntry> log() throws ScmException
    {
        return log(null, null, -1);
    }

    public List<GitLogEntry> log(int changes) throws ScmException
    {
        return log(null, null, changes);
    }

    public List<GitLogEntry> log(String from, String to) throws ScmException
    {
        return log(from, to, -1);
    }

    public List<GitLogEntry> log(String from, String to, int changes) throws ScmException
    {
        List<String> command = new LinkedList<String>();
        command.add(GIT);
        command.add(COMMAND_LOG);
        command.add(LOG_OPTION_NAME_STATUS);
        command.add(LOG_OPTION_PRETTY + "=format:%H%n%cn%n%ce%n%cd%n%s%n.");
        command.add(LOG_OPTION_REVERSE);
        if (changes != -1)
        {
            command.add(LOG_OPTION_CHANGES);
            command.add(Integer.toString(changes));
        }
        if (from != null && to != null)
        {
            command.add(from + ".." + to);
        }

        LogOutputHandler handler = new LogOutputHandler();

        runWithHandler(handler, null, command.toArray(new String[command.size()]));

        if (handler.getExitCode() != 0)
        {
            throw new ScmException("Git command: " + StringUtils.join(" ", command) + " exited " +
                    "with non zero exit code: " + handler.getExitCode() + ". " + handler.getError());
        }

        return handler.getEntries();
    }

    public void checkout(String branch) throws ScmException
    {
        run(GIT, COMMAND_CHECKOUT, branch);
    }
    
    public void checkout(String branch, String localBranch) throws ScmException
    {
        run(GIT, COMMAND_CHECKOUT, CHECKOUT_OPTION_BRANCH, localBranch, branch);
    }

    public void deleteBranch(String branch) throws ScmException
    {
        run(GIT, COMMAND_BRANCH, BRANCH_OPTION_DELETE, branch);
    }

    public void cretaeBranch(String branch) throws ScmException
    {
        run(GIT, COMMAND_BRANCH, branch);
    }

    public List<GitBranchEntry> branch() throws ScmException
    {
        String[] command = {GIT, COMMAND_BRANCH};

        BranchOutputHandler handler = new BranchOutputHandler();

        runWithHandler(handler, null, command);

        if (handler.getExitCode() != 0)
        {
            throw new ScmException("Git command: " + StringUtils.join(" ", command) + " exited " +
                    "with non zero exit code: " + handler.getExitCode() + ". " + handler.getError());
        }

        return handler.getBranches();
    }

    protected void run(String... commands) throws ScmException
    {
        OutputHandlerAdapter handler = new OutputHandlerAdapter();

        runWithHandler(handler, null, commands);

        if (handler.getExitCode() != 0)
        {
            throw new ScmException("Git command: " + StringUtils.join(" ", commands) + " exited " +
                    "with non zero exit code: " + handler.getExitCode() + ". " + handler.getError());
        }
    }

    protected void runWithHandler(final OutputHandler handler, String input, String... commands) throws ScmException
    {
        if (LOG.isLoggable(Level.FINE))
        {
            LOG.fine(StringUtils.join(" ", commands));
        }

        Process child;

        git.command(commands);

        try
        {
            child = git.start();
        }
        catch (IOException e)
        {
            throw new ScmException("Could not start git process: " + e.getMessage(), e);
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
                throw new ScmException("Error writing to input of git process", e);
            }
        }

        final AtomicBoolean activity = new AtomicBoolean(false);
        AsyncProcess async = new AsyncProcess(child, new LineHandler()
        {
            public void handle(String line, boolean error)
            {
                activity.set(true);
                if (error)
                {
                    handler.handleStderr(line);
                }
                else
                {
                    handler.handleStdout(line);
                }
            }
        }, true);

        try
        {
            long lastActivityTime = System.currentTimeMillis();

            Integer exitCode;
            do
            {
                handler.checkCancelled();
                exitCode = async.waitFor(10, TimeUnit.SECONDS);
                if (activity.getAndSet(false))
                {
                    lastActivityTime = System.currentTimeMillis();
                }
                else
                {
                    long secondsSinceActivity = (System.currentTimeMillis() - lastActivityTime) / 1000;
                    if (secondsSinceActivity >= PROCESS_TIMEOUT)
                    {
                        throw new ScmException("Timing out git process after " + secondsSinceActivity + " seconds of inactivity");
                    }
                }
            }
            while (exitCode == null);

            handler.handleExitCode(exitCode);
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
        catch (IOException e)
        {
            throw new ScmException("Error reading output of git process", e);
        }
        finally
        {
            async.destroy();
        }
    }

    interface OutputHandler
    {
        void handleStdout(String line);

        void handleStderr(String line);

        void handleExitCode(int code) throws ScmException;

        int getExitCode();

        String getError();

        void checkCancelled() throws ScmCancelledException;
    }

    private class OutputHandlerAdapter implements OutputHandler
    {
        private int exitCode;

        private String error;

        public void handleStdout(String line)
        {
            if (scmHandler != null)
            {
                scmHandler.status(line);
            }
        }

        public void handleStderr(String line)
        {
            if (!TextUtils.stringSet(error))
            {
                error = "";
            }
            error = error + line + Constants.LINE_SEPARATOR;
        }

        public String getError()
        {
            return error;
        }

        public void handleExitCode(int code) throws ScmException
        {
            this.exitCode = code;
        }

        public int getExitCode()
        {
            return exitCode;
        }

        public void checkCancelled() throws ScmCancelledException
        {
            if (scmHandler != null)
            {
                scmHandler.checkCancelled();
            }
        }
    }

    /**
     * Read the output from the git log command, interpretting the output.
     * <p/>
     *        e34da05e88de03a4aa5b10b338382f09bbe65d4b
     *        Daniel Ostermeier
     *        daniel@zutubi.com
     *        Sun Sep 28 15:06:49 2008 +1000
     *        removed content from a.txt
     *
     *        M       a.txt
     */
    private class LogOutputHandler extends OutputHandlerAdapter
    {
        private List<GitLogEntry> entries;

        private List<String> lines;

        public LogOutputHandler()
        {
            lines = new LinkedList<String>();
        }

        public void handleStdout(String line)
        {
            lines.add(line);
        }

        /**
         * Get the list of git log entries.  Note that the entries are parsed from the log output
         * when this method is called, so ensure that you wait until the log command is complete
         * before requesting the entries.
         *
         * @return the list of parsed git log entries.
         */
        public List<GitLogEntry> getEntries()
        {
            if (entries == null)
            {
                entries = new LinkedList<GitLogEntry>();

                // a) splitup the individual log entries.
                Iterator<String> i = lines.iterator();
                while (i.hasNext())
                {
                    GitLogEntry logEntry = new GitLogEntry();
                    List<String> raw = new LinkedList<String>();
                    String str;
                    logEntry.setId(str = i.next());
                    raw.add(str);
                    logEntry.setAuthor(str = i.next() + " <" + i.next() + ">");
                    raw.add(str);
                    logEntry.setDateString(str = i.next());
                    try
                    {
                        logEntry.setDate(LOG_DATE_FORMAT.parse(str));
                    }
                    catch (ParseException e)
                    {
                        // noop.
                    }
                    raw.add(str);

                    String comment = "";
                    while (!(str = i.next()).equals("."))
                    {
                        comment += str;
                        raw.add(str);
                    }
                    logEntry.setComment(comment);
                    raw.add(str); // dot.

                    // until newline or until end.
                    while (i.hasNext() && TextUtils.stringSet(str = i.next()))
                    {
                        logEntry.addFileChange(str.substring(1).trim(), str.substring(0, 1));
                        raw.add(str);
                    }
                    
                    logEntry.setRaw(raw);
                    entries.add(logEntry);
                }
            }
            return entries;
        }
    }

    /**
     * Read the output from the git branch command, interpretting the information as
     * necessary.
     */
    private class BranchOutputHandler extends OutputHandlerAdapter
    {
        private List<GitBranchEntry> branches = new LinkedList<GitBranchEntry>();

        public void handleStdout(String line)
        {
            GitBranchEntry entry = new GitBranchEntry(line.startsWith("*"), line.substring(2).trim());
            branches.add(entry);
        }

        public List<GitBranchEntry> getBranches()
        {
            return branches;
        }
    }
}
