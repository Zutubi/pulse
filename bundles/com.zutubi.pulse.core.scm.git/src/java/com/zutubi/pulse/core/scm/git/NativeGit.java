package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.process.api.*;
import com.zutubi.util.Constants;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zutubi.pulse.core.scm.git.GitConstants.*;

/**
 * The native git object is a wrapper around the implementation details for running native git operations.
 */
public class NativeGit
{
    private static final Logger LOG = Logger.getLogger(NativeGit.class);
    private static final Logger LOG_COMMANDS = Logger.getLogger(NativeGit.class.getPackage().getName() + ".commands");

    /**
     * Sentinal value used to detect separate parts of log entries.  A GUID is
     * used to prevent false positives in commit comments.  To be extra
     * paranoid we start with a # as it is difficult to have one in a commit
     * comment (it is a comment character).
     */
    private static final String LOG_SENTINAL = "#5d7bf160-ce21-11de-8a39-0800200c9a66";

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    
    private ScmProcessRunner runner;
    private DateFormat timeFormat = SimpleDateFormat.getDateTimeInstance();
    /**
     * The paths to be included in log requests.
     */
    private List<String> includedPaths = new LinkedList<String>();
    /**
     * The paths to be excluded from log requests.
     */
    private List<String> excludedPaths = new LinkedList<String>();

    /**
     * Creates a git command line wrapper with no inactivity timeout.
     */
    public NativeGit()
    {
        this(0);
    }

    /**
     * Creates a git command line wrapper with the given inactivity timeout.
     *
     * @param inactivityTimeout number of seconds of inactivity (no output)
     *                          after which to timeout a git subprocess 
     */
    public NativeGit(int inactivityTimeout)
    {
        runner = new ScmProcessRunner("git");
        runner.setCharset(CHARSET_UTF8);
        runner.setInactivityTimeout(inactivityTimeout);
    }

    /**
     * Set the working directory in which the native git commands will be run.
     *
     * @param dir working directory must exist.
     */
    public void setWorkingDirectory(File dir)
    {
        if (dir == null || !dir.isDirectory())
        {
            throw new IllegalArgumentException("The working directory must be an existing directory.");
        }
        runner.setDirectory(dir);
    }

    public void init(ScmFeedbackHandler handler) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_INIT);
    }

    public void clone(ScmFeedbackHandler handler, String repository, String dir, boolean mirror) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_CLONE, mirror ? FLAG_MIRROR : FLAG_NO_CHECKOUT, repository, dir);
    }

    public void config(ScmFeedbackHandler handler, String name, boolean value) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_CONFIG, CONFIG_TYPE_BOOLEAN, name, Boolean.toString(value));
    }

    public void remoteAdd(ScmFeedbackHandler handler, String name, String repository, String branch) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_REMOTE, ARG_ADD, FLAG_FETCH, FLAG_TRACK, branch, FLAG_SET_HEAD, branch, name, repository);
    }

    public void merge(ScmFeedbackHandler handler, String remote) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_MERGE, remote);
    }
    
    public void pull(ScmFeedbackHandler handler) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_PULL);
    }

    public void fetch(ScmFeedbackHandler handler) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_FETCH);
    }

    public String revisionParse(String revision) throws ScmException
    {
        OutputCapturingHandler capturingHandler = new OutputCapturingHandler();
        runWithHandler(capturingHandler, null, true, getGitCommand(), COMMAND_REVISION_PARSE, revision);
        return capturingHandler.getSingleOutputLine();
    }

    public String mergeBase(String commit1, String commit2) throws ScmException
    {
        OutputCapturingHandler capturingHandler = new OutputCapturingHandler();
        runWithHandler(capturingHandler, null, true, getGitCommand(), COMMAND_MERGE_BASE, commit1, commit2);
        return capturingHandler.getSingleOutputLine();
    }

    public InputStream show(String revision, String object) throws ScmException
    {
        List<String> commands = new LinkedList<String>();
        commands.add(getGitCommand());
        commands.add(COMMAND_SHOW);
        if (revision == null)
        {
            commands.add(object);
        }
        else
        {
            commands.add(revision + ":" + object);
        }

        ScmOutputCapturingHandler handler = new ScmOutputCapturingHandler(CHARSET_UTF8);
        runWithHandler(handler, null, true, commands.toArray(new String[commands.size()]));

        return new ByteArrayInputStream(handler.getOutput().getBytes());
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
        command.add(getGitCommand());
        command.add(COMMAND_LOG);
        command.add(FLAG_NAME_STATUS);
        command.add(FLAG_SHOW_MERGE_FILES);
        command.add(FLAG_PRETTY + "=format:" + LOG_SENTINAL + "%n%H%n%cn%n%ct%n%s%n%b%n" + LOG_SENTINAL);
        command.add(FLAG_REVERSE);
        if (changes != -1)
        {
            command.add(FLAG_CHANGES);
            command.add(Integer.toString(changes));
        }
        if (to != null)
        {
            if (from == null)
            {
                command.add(to);
            }
            else
            {
                command.add(from + ".." + to);
            }
        }

        LogOutputHandler handler = new LogOutputHandler(includedPaths, excludedPaths);

        runWithHandler(handler, null, true, command.toArray(new String[command.size()]));

        return handler.getEntries();
    }

    public void checkout(ScmFeedbackHandler handler, String branch) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_CHECKOUT, FLAG_FORCE, branch);
    }

    public void checkout(ScmFeedbackHandler handler, String branch, String localBranch) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_CHECKOUT, FLAG_FORCE, FLAG_BRANCH, localBranch, branch);
    }

    public void deleteBranch(String branch) throws ScmException
    {
        run(getGitCommand(), COMMAND_BRANCH, FLAG_DELETE, branch);
    }

    public List<GitBranchEntry> branch() throws ScmException
    {
        String[] command = {getGitCommand(), COMMAND_BRANCH};

        BranchOutputHandler handler = new BranchOutputHandler();

        runWithHandler(handler, null, true, command);

        return handler.getBranches();
    }

    public void diff(ScmFeedbackHandler handler, Revision revA, Revision revB) throws ScmException
    {
        String[] command = {getGitCommand(), COMMAND_DIFF, FLAG_NAME_STATUS, revA.getRevisionString(), revB.getRevisionString()};
        run(handler, command);
    }

    public void lsRemote(ScmLineHandler handler, String repository, String refs) throws ScmException
    {
        runWithHandler(handler, null, true, getGitCommand(), COMMAND_LS_REMOTE, repository, refs);
    }

    public void tag(Revision revision, String name, String message, boolean force) throws ScmException
    {
        List<String> commands = new LinkedList<String>();
        commands.add(getGitCommand());
        commands.add(COMMAND_TAG);
        if (force)
        {
            commands.add(FLAG_FORCE);
        }
        commands.add(FLAG_MESSAGE);
        commands.add(message);
        commands.add(name);
        commands.add(revision.getRevisionString());

        run(commands.toArray(new String[commands.size()]));
    }

    public void push(String repository, String refspec) throws ScmException
    {
        run(getGitCommand(), COMMAND_PUSH, repository, refspec);
    }

    public void apply(ScmFeedbackHandler handler, File patch) throws ScmException
    {
        run(handler, getGitCommand(), COMMAND_APPLY, FLAG_VERBOSE, patch.getAbsolutePath());
    }

    public List<String> getConfig(String name) throws ScmException
    {
        OutputCapturingHandler handler = new OutputCapturingHandler();
        runWithHandler(handler, null, false, getGitCommand(), COMMAND_CONFIG, name);
        return handler.getOutputLines();
    }

    public List<ScmFile> lsTree(String treeish, String path) throws ScmException
    {
        LsTreeOutputHandler handler = new LsTreeOutputHandler();
        runWithHandler(handler, null, true, getGitCommand(), COMMAND_LS_TREE, treeish, path);
        return handler.getFiles();
    }

    public String getSingleConfig(String name) throws ScmException
    {
        List<String> lines = getConfig(name);
        if (lines.size() == 0)
        {
            return null;
        }
        else if (lines.size() == 1)
        {
            String value = lines.get(0).trim();
            if (value.length() == 0)
            {
                value = null;
            }

            return value;
        }
        else
        {
            throw new GitException("Expected a single value for config '" + name + "', got: " + lines);
        }
    }

    public String getSingleConfig(String name, String defaultValue) throws ScmException
    {
        String value = getSingleConfig(name);
        if (value == null)
        {
            value = defaultValue;
        }

        return value;
    }

    protected int run(String... commands) throws ScmException
    {
        return run(null, commands);
    }

    protected int run(ScmFeedbackHandler scmHandler, String... commands) throws ScmException
    {
        ScmLineHandlerSupport handler = new ScmLineHandlerSupport(scmHandler);
        return runWithHandler(handler, null, true, commands);
    }

    protected int runWithHandler(final ScmOutputHandler handler, String input, boolean checkExitCode, String... commands) throws ScmException
    {
        long startTime = System.currentTimeMillis();
        try
        {
            return runner.runProcess(handler, input == null ? null : input.getBytes(), checkExitCode, commands);
        }
        finally
        {
            if (LOG_COMMANDS.isLoggable(Level.FINEST))
            {
                long currentTime = System.currentTimeMillis();
                String workingDirectory = (runner.getDirectory() != null) ? runner.getDirectory().getAbsolutePath() + "\t" : "";
                LOG_COMMANDS.finest(timeFormat.format(new Date(currentTime)) + "\t" + runner.getLastCommandLine() + "\t" + workingDirectory + "\t" + (currentTime - startTime) + "\n");
            }
        }
    }

    protected String getGitCommand()
    {
        return System.getProperty(PROPERTY_GIT_COMMAND, DEFAULT_GIT);
    }

    public void setFilterPaths(List<String> includedPaths, List<String> excludedPaths)
    {
        this.includedPaths = includedPaths;
        this.excludedPaths = excludedPaths;
    }

    /**
     * A simple output handler that just captures stdout and stderr line by line.
     */
    static class OutputCapturingHandler implements ScmLineHandler
    {
        private List<String> outputLines = new LinkedList<String>();
        private List<String> errorLines = new LinkedList<String>();

        public String getSingleOutputLine() throws GitException
        {
            if (outputLines.size() != 1)
            {
                throw new GitException("Expecting single line of output got: " + outputLines);
            }

            String line = outputLines.get(0).trim();
            if (line.length() == 0)
            {
                throw new GitException("Expected non-trivial output");
            }

            return line;
        }

        public List<String> getOutputLines()
        {
            return outputLines;
        }

        public List<String> getErrorLines()
        {
            return errorLines;
        }

        public void handleCommandLine(String line)
        {
        }

        public void handleStdout(String line)
        {
            outputLines.add(line);
        }

        public void handleStderr(String line)
        {
            errorLines.add(line);
        }

        public void handleExitCode(int code) throws GitException
        {
        }

        public void checkCancelled()
        {
        }
    }

    /**
     * Read the output from the git log command, interpreting the output.
     * <p/>
     *        #
     *        e34da05e88de03a4aa5b10b338382f09bbe65d4b
     *        Daniel Ostermeier
     *        Sun Sep 28 15:06:49 2008 +1000
     *        removed content from a.txt
     *        #
     *        M       a.txt
     *
     * This format is generated using --pretty=format:... (see {@link NativeGit#log})
     */
    static class LogOutputHandler extends ScmLineHandlerSupport
    {
        private List<GitLogEntry> entries;

        private List<String> lines;
        private List<String> includedPaths;
        private List<String> excludedPaths;

        public LogOutputHandler()
        {
            this(Collections.<String>emptyList(), Collections.<String>emptyList());
        }

        public LogOutputHandler(List<String> includedPaths, List<String> excludedPaths)
        {
            this.lines = new LinkedList<String>();
            this.includedPaths = includedPaths;
            this.excludedPaths = excludedPaths;
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
         *
         * @throws GitException if there is a problem parsing the log output.
         */
        public List<GitLogEntry> getEntries() throws GitException
        {
            final Predicate<String> changesFilter = new FilterPathsPredicate(this.includedPaths, this.excludedPaths);

            try
            {
                if (entries == null)
                {
                    entries = new LinkedList<GitLogEntry>();

                    Iterator<String> i = lines.iterator();

                    // Skip up to and including initial sentinal
                    while (i.hasNext())
                    {
                        if (i.next().equals(LOG_SENTINAL))
                        {
                            break;
                        }
                    }
                    
                    while (i.hasNext())
                    {
                        GitLogEntry logEntry = new GitLogEntry();
                        List<String> raw = new LinkedList<String>();
                        String str;
                        raw.add(str = i.next());
                        logEntry.setId(str);
                        raw.add(str = i.next());
                        logEntry.setAuthor(str);
                        raw.add(str = i.next());
                        logEntry.setDateString(str);
                        try
                        {
                            logEntry.setDate(new Date(Long.parseLong(str) * Constants.SECOND));
                        }
                        catch (NumberFormatException e)
                        {
                            LOG.severe("Failed to parse the timestamp: '" + str + "'");
                            LOG.severe("The log output received was:\n" + StringUtils.join("\n", lines));
                        }

                        String comment = "";
                        while (i.hasNext() && !(str = i.next()).equals(LOG_SENTINAL))
                        {
                            if (comment.length() > 0)
                            {
                                comment += "\n";
                            }
                            comment += str;
                            raw.add(str);
                        }
                        logEntry.setComment(comment.trim());
                        raw.add(str);

                        // Until sentinal or until the end.  Note that most of
                        // the time a blank line appears at the end of the
                        // files, but we use the sentinal as a more reliable
                        // way to detect termination.
                        boolean logEntryHasFiles = false;
                        while (i.hasNext() && !(str = i.next()).equals(LOG_SENTINAL))
                        {
                            String[] parts = str.split("\\s+", 2);
                            if (parts.length == 2)
                            {
                                logEntryHasFiles = true;
                                if (changesFilter.satisfied(parts[1]))
                                {
                                    logEntry.addFileChange(parts[1], parts[0]);
                                }
                            }
                            raw.add(str);
                        }

                        logEntry.setRaw(raw);
                        if (!logEntryHasFiles || logEntry.getFiles().size() > 0)
                        {
                            // Either the log entry has no files, in which case none were filtered, or
                            // it has some files, not all of which were filtered.
                            entries.add(logEntry);
                        }
                    }
                }
                return entries;
            }
            catch (Exception e)
            {
                // print some debugging output.
                LOG.severe("A problem has occurred whilst parsing the git log output: " + e.getMessage(), e);
                LOG.severe("The log output received was:\n" + StringUtils.join("\n", lines));

                throw new GitException(e);
            }
        }
    }

    /**
     * Read the output from the git branch command, interpretting the information as
     * necessary.
     */
    private class BranchOutputHandler extends ScmLineHandlerSupport
    {
        private List<GitBranchEntry> branches = new LinkedList<GitBranchEntry>();

        private final Pattern BRANCH_OUTPUT = Pattern.compile("\\*?\\s+(.+)");

        public void handleStdout(String line)
        {
            Matcher matcher = BRANCH_OUTPUT.matcher(line);
            if (matcher.matches())
            {
                GitBranchEntry entry = new GitBranchEntry(line.startsWith("*"), matcher.group(1).trim());
                branches.add(entry);
            }
        }

        public List<GitBranchEntry> getBranches()
        {
            return branches;
        }
    }

    static class LsTreeOutputHandler extends ScmLineHandlerSupport
    {
        private List<ScmFile> files = new LinkedList<ScmFile>();

        @Override
        public void handleStdout(String line)
        {
            String[] parts = line.split("\\s+", 4);
            if (parts.length == 4)
            {
                files.add(new ScmFile(parts[3], parts[1].trim().equals(TYPE_TREE)));
            }
        }

        public List<ScmFile> getFiles()
        {
            return files;
        }
    }
    /**
     * Provide command line style access to running git commands for testing.
     * @param argv command line arguments
     * @throws IOException if an error occurs.
     */
    public static void main(String... argv) throws IOException
    {
        if (argv.length == 0)
        {
            System.out.println("Please enter the full git command you with to execute.");
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
            NativeGit git = new NativeGit();
            git.setWorkingDirectory(new File("."));
            System.out.println(new File(".").getCanonicalPath());
            if (!Boolean.getBoolean("skip.env"))
            {
                // use a tree map to provide ordering to the keys.
                System.out.println("========= Execution Environment ============");
                Map<String, String> env = new TreeMap<String, String>(git.runner.getEnvironment());
                for (String key : env.keySet())
                {
                    String value = env.get(key);
                    System.out.println(key + "=" + value);
                }
                System.out.println();
                System.out.println("========= Command output ============");
                System.out.println(StringUtils.join(" ", argv));
            }
            git.runWithHandler(outputHandler, null, true, argv);
        }
        catch (ScmException e)
        {
            System.out.println("Exit Status: " + outputHandler.getExitCode());
            e.printStackTrace();
        }
    }
}
