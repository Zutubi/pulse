package com.zutubi.pulse.core.scm.git;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.core.scm.ScmCancelledException;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.util.process.AsyncProcess;
import com.zutubi.pulse.util.process.LineHandler;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private ScmEventHandler scmHandler;

    private ProcessBuilder git;

    private static final String GIT = "git";
    private static final String COMMAND_PULL = "pull";
    private static final String COMMAND_LOG = "log";
    private static final String COMMAND_CLONE = "clone";
    private static final String COMMAND_CHECKOUT = "checkout";
    private static final String COMMAND_BRANCH = "branch";
    private static final String COMMAND_SHOW = "show";
    private static final String CHECKOUT_OPTION_BRANCH = "-b";
    private static final String LOG_OPTION_STAT = "--stat";
    private static final String LOG_OPTION_SUMMARY = "--summary";
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

    public void setScmEventHandler(ScmEventHandler scmHandler)
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
        command.add(LOG_OPTION_STAT);
        command.add(LOG_OPTION_SUMMARY);
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
     * Sample output:
     * <p/>
     * commit d8fba19342690182dc7fd032dbc71eb8541966da
     * Author: Daniel Ostermeier <daniel@zutubi.com>
     * Date:   Tue Sep 23 18:10:12 2008 +1000
     * <p/>
     * update 8
     * <p/>
     * file.txt |    7 +------
     * new.txt  |    1 +
     * 2 files changed, 2 insertions(+), 6 deletions(-)
     * create mode 100755 b.txt
     * delete mode 100755 a.txt
     */
    private class LogOutputHandler extends OutputHandlerAdapter
    {
        private static final String COMMIT_TAG = "commit";
        private static final String AUTHOR_TAG = "Author:";
        private static final String DATE_TAG = "Date:";

        private final Pattern commitPattern = Pattern.compile("commit (.*)");
        private final Pattern authorPattern = Pattern.compile("Author:(.*)");
        private final Pattern datePattern = Pattern.compile("Date:(.*)");
        private final Pattern filesPattern = Pattern.compile("(.*)[ ]*\\|.*");
        private final Pattern summaryPattern = Pattern.compile(" \\d+ files changed, \\d+ insertions\\(\\+\\), \\d+ deletions\\(-\\)");
        private final Pattern createDeletePattern = Pattern.compile(" (.+?) mode \\d+ (.*)");

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
                List<List<String>> individualEntries = new LinkedList<List<String>>();
                List<String> currentEntry = null;
                for (String line : lines)
                {
                    if (commitPattern.matcher(line).matches())
                    {
                        if (currentEntry != null)
                        {
                            individualEntries.add(currentEntry);
                        }
                        currentEntry = new LinkedList<String>();
                    }
                    if (currentEntry != null)
                    {
                        currentEntry.add(line);
                    }
                }
                if (currentEntry != null)
                {
                    individualEntries.add(currentEntry);
                }

                // process the individual entries.
                for (List<String> entry : individualEntries)
                {
                    Matcher m;
                    Iterator<String> iterator = entry.iterator();
                    String str = iterator.next();

                    GitLogEntry logEntry = new GitLogEntry();
                    logEntry.setId(str.substring(COMMIT_TAG.length()).trim());
                    str = iterator.next();
                    if (authorPattern.matcher(str).matches())
                    {
                        logEntry.setAuthor(str.substring(AUTHOR_TAG.length()).trim());
                        str = iterator.next();
                    }
                    else
                    {
                        throw new RuntimeException();
                    }

                    if (datePattern.matcher(str).matches())
                    {
                        try
                        {

                            String dateString = str.substring(DATE_TAG.length()).trim();
                            logEntry.setDateString(dateString);
                            logEntry.setDate(LOG_DATE_FORMAT.parse(dateString));
                        }
                        catch (ParseException e)
                        {
                            LOG.warning(e);
                        }
                        str = iterator.next();
                    }
                    else
                    {
                        throw new RuntimeException();
                    }

                    // comment
                    String comment = str;
                    while (iterator.hasNext())
                    {
                        str = iterator.next();
                        comment = comment + str;
                        if (!TextUtils.stringSet(str))
                        {
                            break;
                        }
                    }
                    logEntry.setComment(comment);

                    if (!TextUtils.stringSet(str))
                    {
                        if (iterator.hasNext())
                        {
                            str = iterator.next();
                        }
                    }

                    while ((m = filesPattern.matcher(str)).matches())
                    {
                        // files.
                        String file = m.group(1);
                        logEntry.addFileChange(file, "update");
                        if (iterator.hasNext())
                        {
                            str = iterator.next();
                        }
                        else
                        {
                            break;
                        }
                    }
                    if (summaryPattern.matcher(str).matches())
                    {
                        // summary.
                        if (iterator.hasNext())
                        {
                            str = iterator.next();
                        }
                    }

                    while ((m = createDeletePattern.matcher(str)).matches())
                    {
                        String action = m.group(1);
                        String file = m.group(2);
                        logEntry.addFileChange(file, action);

                        if (iterator.hasNext())
                        {
                            str = iterator.next();
                        }
                        else
                        {
                            break;
                        }
                    }

                    logEntry.setRaw(entry);
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
