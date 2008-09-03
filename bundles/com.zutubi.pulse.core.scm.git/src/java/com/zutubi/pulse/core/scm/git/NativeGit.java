package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.LineHandler;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class NativeGit
{
    private static final Logger LOG = Logger.getLogger(NativeGit.class);

    private static final long PROCESS_TIMEOUT = Long.getLong("pulse.git.inactivity.timeout", 300);

    private static final String ASCII_CHARSET = "US-ASCII";

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
        runWithHandler(new NoopOutputHandler(), null, "git", "clone", repository, dir);
    }

    public void pull() throws ScmException
    {
        runWithHandler(new NoopOutputHandler(), null, "git", "pull");
    }

    public List<GitLogEntry> log(String from, String to) throws ScmException
    {
        LogOutputHandler handler = new LogOutputHandler();
        
        runWithHandler(handler, null, "git", "log", from+".."+to);

        return handler.getEntries();
    }

    public void runWithHandler(final OutputHandler handler, String input, String... commands) throws ScmException
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

        void checkCancelled() throws ScmCancelledException;
    }

    private class OutputHandlerAdapter implements OutputHandler
    {
        public void handleStdout(String line)
        {

        }

        public void handleStderr(String line)
        {

        }

        public void handleExitCode(int code) throws ScmException
        {

        }

        public void checkCancelled() throws ScmCancelledException
        {

        }
    }

    private class NoopOutputHandler extends OutputHandlerAdapter
    {
        public void handleStdout(String line)
        {
            System.out.println("handleStdout: " + line);
        }

        public void handleStderr(String line)
        {
            System.out.println("handleStderr: " + line);
        }

        public void handleExitCode(int code) throws ScmException
        {
            System.out.println("handleExitCode: " + code);
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
                currentEntry.setCommit(line.substring(8).trim());
            }
            else if (line.startsWith("Date:   "))
            {
                currentEntry.setCommit(line.substring(8).trim());
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
}
