package com.cinnamonbob.shell;

import com.cinnamonbob.util.RandomUtils;
import com.cinnamonbob.util.Constants;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

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

    private StdOutErrParser stdOutReader;

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

            stdOutReader = new StdOutErrParser(process.getInputStream(), output);
            stdOutReader.start();

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
        stdOutReader.setCommandTerminationString(randomString);

        internalExecute(cmd);

        // determine exit status of command... this may not always be possible.
        internalExecute("echo " + randomString + " " + getExitStatusVariable());
    }

    public InputStream getInput()
    {
        return input;
    }

    public int getExitStatus()
    {
        return stdOutReader.getExitStatus();
    }

    public void waitFor()
    {
        stdOutReader.waitFor();
    }

    public void waitFor(long millis)
    {
        stdOutReader.waitFor(millis);
    }

    public void kill()
    {
        process.destroy();
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
}


/**
 *
 */
class StdOutErrParser extends Thread
{
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(StdOutErrParser.class.getName());

    /**
     * The standard out / standard error stream from the shell process.
     */
    private final InputStream input;

    /**
     * The output stream to which this reader writes data it receives from the shell process.
     */
    private final OutputStream output;

    private String commandTerminationString = null;

    private boolean commandComplete;

    private int commandExitStatus;

    //---(  )---

    public static final int IDLE = 0;
    public static final int EXECUTING = 1;
    public static final int COMPLETE = 2;

    private int status = IDLE;

    /**
     * @param input
     * @param output
     */
    protected StdOutErrParser(InputStream input, OutputStream output)
    {
        this.input = input;
        this.output = output;
    }

    protected void setCommandTerminationString(String str)
    {
        commandTerminationString = str;
        commandComplete = false;
        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
    }

    protected synchronized void waitFor()
    {
        while (!commandComplete)
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
     *
     */
    public void run()
    {
        try
        {
            OutputStreamWriter writer = new OutputStreamWriter(output);
            BufferedReader br = new BufferedReader(new InputStreamReader(input));

            String line;
            while ((line = br.readLine()) != null)
            {
                // look for command termination string.
                if (line.contains(commandTerminationString))
                {
                    if (line.startsWith(commandTerminationString))
                    {
                        synchronized (this)
                                {
                                    commandComplete = true;
                                    String exitStatus = line.substring(commandTerminationString.length() + 1);
                                    try
                                    {
                                        commandExitStatus = Integer.parseInt(exitStatus);
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
                                    }
                                    writer.write(Shell.END_OF_COMMAND);
                                    writer.flush();
                                    notifyAll();
                                }
                    }
                    // hack to make the out put a little cleaner..
                    else if (line.contains("echo " + commandTerminationString))
                    {
                        // ignore it, its just windows echoing the 'echo' command.
                    } else
                    {
                        // legit output.
                        writer.write(line);
                        writer.write(Constants.LINE_SEPARATOR);
                    }
                } else
                {
                    writer.write(line);
                    writer.write(Constants.LINE_SEPARATOR);
                }
            }
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            LOG.log(Level.SEVERE, "Error reading input.", e);
        }
        finally
        {
            // make sure that we notify any threads currently waiting.
            synchronized (this)
                    {
                        commandComplete = true;
                        commandExitStatus = Shell.EXIT_STATUS_UNKNOWN;
                        notifyAll();
                    }
        }
    }

    public int getExitStatus()
    {
        return commandExitStatus;
    }

    public synchronized void waitFor(long millis)
    {
        try
        {
            wait(millis);
        }
        catch (InterruptedException e)
        {
            // noop.
        }
    }
}

