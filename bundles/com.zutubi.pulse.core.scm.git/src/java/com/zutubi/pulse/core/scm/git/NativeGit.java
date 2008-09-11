package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.LineHandler;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.List;
import java.util.LinkedList;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 *
 *
 */
public class NativeGit
{
    private static final Logger LOG = Logger.getLogger(NativeGit.class);

    private static final long PROCESS_TIMEOUT = Long.getLong("pulse.git.inactivity.timeout", 300);

    private static final String ASCII_CHARSET = "US-ASCII";

    private final static SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy z");

    private ProcessBuilder git;

    public NativeGit()
    {
        git = new ProcessBuilder();
    }

    public void setWorkingDirectory(File dir)
    {
        git.directory(dir);
    }

    public void clone(String repository, String dir) throws ScmException
    {
        String[] command = {"git", "clone", repository, dir};

        run(command);
    }

    public void pull() throws ScmException
    {
        String[] command = {"git", "pull"};
        
        run(command);
    }

    public void fetch(String remote) throws ScmException
    {
        String[] command = {"git", "fetch", remote};

        run(command);
    }

    public List<GitLogEntry> log(String from, String to) throws ScmException
    {
        String[] command = {"git", "log", from+".."+to};

        LogOutputHandler handler = new LogOutputHandler();
        
        runWithHandler(handler, null, command);

        if (handler.getExitCode() != 0)
        {
            LOG.warning("Git command: " + StringUtils.join(" ", command) + " exited " +
                    "with non zero exit code: " + handler.getExitCode());
            LOG.warning(handler.getError());
        }

        return handler.getEntries();
    }

    public void checkout(String branch) throws ScmException
    {
        String[] command = {"git", "checkout", branch};

        run(command);
    }

    public List<GitBranchEntry> branch() throws ScmException
    {
        String[] command = {"git", "branch", "-r"};

        BranchOutputHandler handler = new BranchOutputHandler();

        runWithHandler(handler, null, command);

        if (handler.getExitCode() != 0)
        {
            LOG.warning("Git command: " + StringUtils.join(" ", command) + " exited " +
                    "with non zero exit code: " + handler.getExitCode());
            LOG.warning(handler.getError());
        }

        return handler.getBranches();
    }

    protected void run(String... commands) throws ScmException
    {
        OutputHandlerAdapter handler = new OutputHandlerAdapter();

        runWithHandler(handler, null, commands);

        if (handler.getExitCode() != 0)
        {
            LOG.warning("Git command: " + StringUtils.join(" ", commands) + " exited " +
                    "with non zero exit code: " + handler.getExitCode());
            LOG.warning(handler.getError());
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
                throw new ScmException("Error writing to input of p4 process", e);
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
                        throw new ScmException("Timing out p4 process after " + secondsSinceActivity + " seconds of inactivity");
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
            throw new ScmException("Error reading output of p4 process", e);
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

        void checkCancelled() throws ScmCancelledException;
    }

    private class OutputHandlerAdapter implements OutputHandler
    {
        private int exitCode;
        
        private String error;

        public void handleStdout(String line)
        {

        }

        public void handleStderr(String line)
        {
            if (!TextUtils.stringSet(error))
            {
                error = "";
            }
            error = error + line + "\n";
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

        }
    }

    private class LogOutputHandler extends OutputHandlerAdapter
    {
        private List<GitLogEntry> entries = new LinkedList<GitLogEntry>();
        
        private GitLogEntry currentEntry;

        public void handleStdout(String line)
        {
            if (line.startsWith("commit "))
            {
                currentEntry = new GitLogEntry();
                entries.add(currentEntry);
                currentEntry.setCommit(line.substring(7).trim());
            }
            else if (line.startsWith("Author: "))
            {
                currentEntry.setAuthor(line.substring(8).trim());
            }
            else if (line.startsWith("Date:   "))
            {
                String dtStr = line.substring(8).trim();
                try
                {
                    currentEntry.setDate(LOG_DATE_FORMAT.parse(dtStr));
                }
                catch (ParseException e)
                {
                    LOG.warning(e);
                }
            }
            else
            {
                //TODO: improve handling of comment - it seems to be 
                //TODO: preformatted - remove preformatting if practical.
                currentEntry.setComment(currentEntry.getComment() + line);
            }
        }

        public List<GitLogEntry> getEntries()
        {
            return entries;
        }
    }

    private class BranchOutputHandler extends OutputHandlerAdapter
    {
        private List<GitBranchEntry> branches = new LinkedList<GitBranchEntry>();

        public void handleStdout(String line)
        {
            GitBranchEntry entry = new GitBranchEntry();
            if (line.startsWith("*"))
            {
                entry.setActive(true);
                line = line.substring(2);
            }
            entry.setName(line.trim());
            branches.add(entry);
        }

        public List<GitBranchEntry> getBranches()
        {
            return branches;
        }
    }
}
