package com.cinnamonbob.shell;

import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.RandomUtils;
import com.cinnamonbob.util.logging.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The Shell provides a native command execution environment.
 */
public class Shell
{
    /**
     * This indicates that the command exit status is unknown.
     */
    public static final int EXIT_STATUS_UNKNOWN = Integer.MIN_VALUE;

    public static final char END_OF_COMMAND = '\u0003';
    public static final char EOC = END_OF_COMMAND;

    /**
     * Indicates the shells open status.
     */
    private boolean isOpen;

    private StdOutErrParser stdOutParser;

    private PrintWriter writer;

    private Process process;

    private final NativeShellConfiguration config = new NativeShellConfiguration();

    /**
     *
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    private String path;

    private File directory;

    private PipedInputStream input;

    public Shell()
    {
        // initialise the environment.
        environment.putAll(System.getenv());
    }

    public Map<String, String> getEnvironment()
    {
        return this.environment;
    }

    public void addPath(String path)
    {
        if (this.path == null)
        {
            this.path = "";
        }
        this.path = this.path + File.pathSeparator + path;
    }

    /**
     * Set the initial working directory of the native shell.
     *
     * @param directory
     */
    //TODO: Need to set a default for the initial working directory.
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }

    /**
     * @return true if this shell has been opened and is currently open, false otherwise.
     * @see #open()
     * @see #close()
     */
    public boolean isOpen()
    {
        return isOpen;
    }

    public boolean isExecuting()
    {
        return stdOutParser.isExecuting();
    }

    public boolean isIdle()
    {
        return stdOutParser.isIdle();
    }

    /**
     * @throws IOException
     * @throws IllegalStateException if this shell is already open.
     * @see #isOpen()
     */
    public void open() throws IOException
    {
        if (isOpen())
        {
            throw new IllegalStateException("Shell is already open.");
        }

        try
        {
            ProcessBuilder builder = new ProcessBuilder(getOpenShellCommand());
            builder.redirectErrorStream(true);
            if (directory != null)
            {
                builder.directory(directory);
            }

            // update environment and path.
            Map<String, String> env = builder.environment();
            env.clear();
            env.putAll(environment);
            if (path != null)
            {
                addPath(env.get(config.getPathVariable()));
                env.put(config.getPathVariable(), path);
            }

            process = builder.start();

            input = new PipedInputStream();
            // will be cleaned up by the StdOutErrParser during its cleanup.
            PipedOutputStream output = new PipedOutputStream(input);

            stdOutParser = new StdOutErrParser(process.getInputStream(), output);
            stdOutParser.start();

            writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));

            isOpen = true;
        }
        finally
        {
            if (!isOpen)
            {
                // ensure that we close any resources that we have opened
                if (process != null)
                {
                    process.destroy();
                }
            }
        }
    }

    /**
     * Execute the specified command within a native os command shell.
     * <p/>
     * This method will block until the execution of the command has been completed.
     *
     * @param cmd
     * @throws IllegalStateException if this shell is not open
     * @see #isOpen()
     * @see #open()
     */
    public void execute(String cmd) throws InterruptedException
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Shell must be opened before it can execute commands.");
        }

        // use the random string to 'tag' the end of the command, allowing us to detect
        // when a command has finished.
        String randomString = RandomUtils.randomString(10);
        stdOutParser.addCommand(randomString);

        internalExecute(cmd);

        // determine exit status of command... this may not always be possible.
        internalExecute(getEchoCommand() + " " + randomString + " " + getExitStatusVariable());
    }

    public InputStream getInput()
    {
        return input;
    }

    public int getExitStatus()
    {
        return stdOutParser.getExitStatus();
    }

    public void waitFor()
    {
        stdOutParser.waitFor();
    }

    public void waitFor(long millis)
    {
        stdOutParser.waitFor(millis);
    }

    public void kill()
    {
        process.destroy();
        stdOutParser.interrupt();
        isOpen = false;
    }

    private void internalExecute(String command)
    {
        writer.write(command + Constants.LINE_SEPARATOR);
        writer.flush();
    }

    /**
     * @throws InterruptedException
     */
    public void close() throws InterruptedException
    {
        if (!isOpen())
        {
            throw new IllegalStateException("Shell is not open.");
        }

        internalExecute(getCloseShellCommand());

        // the process exiting has a number of important side effects.
        // a) stdout/stderr/stdin streams close.
        // b) StdOutStdErrReader threads input stream closes, so it exits.
        // c) stdouterr thread closes its end of the PipedStream resulting in
        //    EOF for anyone reading from the input end.
        // so, everything is closed.
        if (process != null)
        {
            try
            {
                int shellExitStatus = process.waitFor();
            }
            catch (InterruptedException e)
            {
                // noop.
            }
            finally
            {
                process = null;
            }
        }

        isOpen = false;
    }

    public String getOpenShellCommand()
    {
        return config.getOpenShellCommand();
    }

    public String getCloseShellCommand()
    {
        return config.getCloseShellCommand();
    }

    public String getExitStatusVariable()
    {
        return config.getExitStatusVariable();
    }

    public String getEchoCommand()
    {
        return config.getEchoCommand();
    }
}


/**
 *
 */
class StdOutErrParser extends Thread
{
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(StdOutErrParser.class);

    /**
     *
     */
    private final BufferedReader input;

    /**
     *
     */
    private final Writer output;

    private LinkedList<String> cmdEndMarkers = new LinkedList<String>();

    private int commandExitStatus;

    /**
     * @param input
     * @param output
     */
    protected StdOutErrParser(InputStream input, OutputStream output)
    {
        this.output = new OutputStreamWriter(output);
        this.input = new BufferedReader(new InputStreamReader(input));
    }

    protected void addCommand(String cmdEndMarker)
    {
        cmdEndMarkers.addLast(cmdEndMarker);
        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
    }

    /**
     * Wait for the currently executing command to complete. This method will
     * block until the currently executing command is finished. If no command
     * is executing, this method will return immediately.
     */
    protected synchronized void waitFor()
    {
        while (isExecuting())
        {
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }
    }

    /**
     * Wait for the currently executing command to complete. This method will
     * block until either the currently executing command is finished OR the
     * specified timeout expires. If no command is executing, this method will
     * return immediately.
     *
     * @param timeout
     */
    public synchronized void waitFor(long timeout)
    {
        final long finish = System.currentTimeMillis() + timeout;
        while (isExecuting())
        {
            try
            {
                long now = System.currentTimeMillis();
                long diff = finish - now;
                if (diff <= 0)
                {
                    return;
                }
                wait(diff);
            }
            catch (InterruptedException e)
            {
                return;
            }
        }
    }

    public boolean isIdle()
    {
        return !isExecuting();
    }

    public boolean isExecuting()
    {
        return cmdEndMarkers.size() > 0;
    }

    /**
     *
     */
    public void run()
    {
        try
        {
            String line;
            while ((line = input.readLine()) != null)
            {
                if (cmdEndMarkers.size() == 0)
                {
                    writeLineToOutput(line);
                    continue;
                }

                String nextCmdEndMarker = (String) cmdEndMarkers.getFirst();

                // look for command termination string.
                if (line.contains(nextCmdEndMarker))
                {
                    if (line.startsWith(nextCmdEndMarker))
                    {
                        commandComplete(line);
                    }
                    // hack to make the out put a little cleaner..
                    else if (line.contains("echo " + nextCmdEndMarker))
                    {
                        // ignore it, its just windows echoing the 'echo' command.
                    }
                    else
                    {
                        writeLineToOutput(line);
                    }
                }
                else
                {
                    writeLineToOutput(line);
                }
            }
        }
        catch (IOException e)
        {
            LOG.severe("Error reading input.", e);
            // need to clear up the cmd end markers so that we ensure that
            // any waiting clients will be released.
            cmdEndMarkers.clear();
        }
        finally
        {
            // make sure that we notify any threads currently waiting.
            synchronized (this)
            {
                IOUtils.close(output);
                if (isExecuting())
                {
                    LOG.severe("finalizing parser but still waiting on commands to complete???");
                    cmdEndMarkers.clear();
                }
                notifyAll();
            }
        }
    }

    private synchronized void commandComplete(String line) throws IOException
    {
        // update the state of the parser.
        String marker = (String) cmdEndMarkers.removeFirst();

        // read the command exit status.
        String exitStatus = line.substring(marker.length() + 1);
        try
        {
            commandExitStatus = Integer.parseInt(exitStatus);
        }
        catch (NumberFormatException e)
        {
            commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
        }

        // write END OF COMMAND to output stream.
        try
        {
            output.write(Shell.END_OF_COMMAND);
            output.flush();
        }
        finally
        {
            notifyAll();
        }
    }

    private void writeLineToOutput(String line) throws IOException
    {
        output.write(line);
        output.write(Constants.LINE_SEPARATOR);
    }

    /**
     * Get the exit status of the most recently completed command.
     *
     * @return
     */
    public int getExitStatus()
    {
        return commandExitStatus;
    }


}

